package org.sfa.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sfa.request.dto.OtpIssueRequestDTO;
import org.sfa.request.dto.OtpIssueResultDTO;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationEvent;
import org.sfa.request.dto.notification.NotificationEventType;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.exception.types.OtpDeliveryFailedException;
import org.sfa.request.exception.types.OtpRecipientUnreachableException;
import org.sfa.request.model.entity.OtpVerification;
import org.sfa.request.model.enums.OtpParty;
import org.sfa.request.model.enums.OtpVerificationState;
import org.sfa.request.repository.OtpVerificationRepository;
import org.sfa.request.repository.UserContactRepository;
import org.sfa.request.repository.projection.UserContactRow;
import org.sfa.request.service.api.EmailNotificationService;
import org.sfa.request.service.api.OtpIssueService;
import org.sfa.request.service.api.SnsNotificationService;
import org.sfa.request.service.api.WorkflowCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Steps S1–S3 of Part 3 (OTP verification, issue #95).
 *
 * <ol>
 *   <li>Look up both parties' contact details. Fail before changing anything if either
 *       has neither a phone number nor an email address.</li>
 *   <li>Mark any earlier PENDING record for the request SUPERSEDED (lead replaced, or a
 *       retry of this step).</li>
 *   <li>Generate two distinct codes; store only their HMACs, the per-code attempt
 *       counters, the expiry and the workflow's task token.</li>
 *   <li>Resume any superseded execution that is still paused, with
 *       {@code {"result": "SUPERSEDED"}}, so it ends instead of timing out into
 *       reassignment.</li>
 *   <li>Send each party <em>their own</em> code by SMS and/or email.</li>
 * </ol>
 *
 * <p>Codes are sent directly through SNS/SES: never through the SQS notification queue
 * or the in-app notifications table, so they exist only in the recipient's message.
 * Codes and task tokens are never logged.</p>
 *
 * <p>The steps are not wrapped in one transaction. If this step fails part-way, the
 * workflow's retry runs it again, which supersedes whatever the failed attempt left
 * behind and issues fresh codes.</p>
 */
@Slf4j
@Service
public class OtpIssueServiceImpl implements OtpIssueService {

    static final String SUPERSEDED_RESULT_KEY = "result";

    private static final DateTimeFormatter EXPIRY_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm 'UTC'");

    private final OtpVerificationRepository otpVerificationRepository;
    private final UserContactRepository userContactRepository;
    private final OtpCodeGenerator codeGenerator;
    private final OtpCodeHasher codeHasher;
    private final SnsNotificationService snsNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final WorkflowCallbackService workflowCallbackService;
    private final int validityMinutes;
    private final int maxAttemptsPerCode;
    private final Clock clock;

    @Autowired
    public OtpIssueServiceImpl(OtpVerificationRepository otpVerificationRepository,
                               UserContactRepository userContactRepository,
                               OtpCodeGenerator codeGenerator,
                               OtpCodeHasher codeHasher,
                               SnsNotificationService snsNotificationService,
                               EmailNotificationService emailNotificationService,
                               WorkflowCallbackService workflowCallbackService,
                               @Value("${saayam.otp.validity-minutes:1440}") int validityMinutes,
                               @Value("${saayam.otp.max-attempts-per-code:3}") int maxAttemptsPerCode) {
        this(otpVerificationRepository, userContactRepository, codeGenerator, codeHasher,
                snsNotificationService, emailNotificationService, workflowCallbackService,
                validityMinutes, maxAttemptsPerCode, Clock.systemUTC());
    }

    /** Used by tests to fix the clock. */
    OtpIssueServiceImpl(OtpVerificationRepository otpVerificationRepository,
                        UserContactRepository userContactRepository,
                        OtpCodeGenerator codeGenerator,
                        OtpCodeHasher codeHasher,
                        SnsNotificationService snsNotificationService,
                        EmailNotificationService emailNotificationService,
                        WorkflowCallbackService workflowCallbackService,
                        int validityMinutes,
                        int maxAttemptsPerCode,
                        Clock clock) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.userContactRepository = userContactRepository;
        this.codeGenerator = codeGenerator;
        this.codeHasher = codeHasher;
        this.snsNotificationService = snsNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.workflowCallbackService = workflowCallbackService;
        this.validityMinutes = validityMinutes;
        this.maxAttemptsPerCode = maxAttemptsPerCode;
        this.clock = clock;
    }

    @Override
    public OtpIssueResultDTO issueAndSend(OtpIssueRequestDTO request) {
        validate(request);

        UserContactRow requesterContact = reachableContact(OtpParty.REQUESTER, request.getRequesterId());
        UserContactRow volunteerContact = reachableContact(OtpParty.LEAD_VOLUNTEER, request.getLeadVolunteerId());

        ZonedDateTime now = ZonedDateTime.now(clock);
        List<OtpVerification> superseded = supersedePending(request.getRequestId(), now);

        String otpVerificationId = UUID.randomUUID().toString();
        String[] codes = codeGenerator.generateDistinctPair();
        String requesterCode = codes[0];
        String volunteerCode = codes[1];
        ZonedDateTime expiresAt = now.plusMinutes(validityMinutes);

        otpVerificationRepository.save(OtpVerification.builder()
                .otpVerificationId(otpVerificationId)
                .requestId(request.getRequestId())
                .requesterId(request.getRequesterId())
                .leadVolunteerId(request.getLeadVolunteerId())
                .requesterCodeHash(codeHasher.hash(otpVerificationId, OtpParty.REQUESTER, requesterCode))
                .volunteerCodeHash(codeHasher.hash(otpVerificationId, OtpParty.LEAD_VOLUNTEER, volunteerCode))
                .requesterCodeAttemptsRemaining(maxAttemptsPerCode)
                .volunteerCodeAttemptsRemaining(maxAttemptsPerCode)
                .requesterCodeVerified(false)
                .volunteerCodeVerified(false)
                .state(OtpVerificationState.PENDING.name())
                .expiresAt(expiresAt)
                .taskToken(request.getTaskToken())
                .createdAt(now)
                .lastUpdateDate(now)
                .build());

        resumeSuperseded(superseded, request.getTaskToken());

        // Fail fast: if the requester cannot be reached, don't send the volunteer a code
        // that the retry would immediately supersede.
        deliver(OtpParty.REQUESTER, requesterContact, request.getRequestId(), requesterCode, expiresAt);
        deliver(OtpParty.LEAD_VOLUNTEER, volunteerContact, request.getRequestId(), volunteerCode, expiresAt);

        log.info("Issued OTP verification {} for request {} ({} earlier record(s) superseded)",
                otpVerificationId, request.getRequestId(), superseded.size());

        return OtpIssueResultDTO.builder()
                .otpVerificationId(otpVerificationId)
                .requestId(request.getRequestId())
                .expiresAt(expiresAt)
                .supersededCount(superseded.size())
                .build();
    }

    private void validate(OtpIssueRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        requireText(request.getRequestId(), "requestId");
        requireText(request.getRequesterId(), "requesterId");
        requireText(request.getLeadVolunteerId(), "leadVolunteerId");
        requireText(request.getTaskToken(), "taskToken");
        if (validityMinutes < 1) {
            throw new IllegalStateException("saayam.otp.validity-minutes must be at least 1");
        }
        if (maxAttemptsPerCode < 1) {
            throw new IllegalStateException("saayam.otp.max-attempts-per-code must be at least 1");
        }
    }

    private static void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private UserContactRow reachableContact(OtpParty party, String userId) {
        return userContactRepository.findContactByUserId(userId)
                .filter(c -> StringUtils.hasText(c.getPhoneNumber()) || StringUtils.hasText(c.getEmailAddress()))
                .orElseThrow(() -> new OtpRecipientUnreachableException(party, userId));
    }

    /** Marks earlier PENDING records for the request SUPERSEDED and returns them. */
    private List<OtpVerification> supersedePending(String requestId, ZonedDateTime now) {
        List<OtpVerification> pending =
                otpVerificationRepository.findByRequestIdAndState(requestId, OtpVerificationState.PENDING.name());
        if (pending.isEmpty()) {
            return List.of();
        }
        for (OtpVerification record : pending) {
            record.setState(OtpVerificationState.SUPERSEDED.name());
            record.setLastUpdateDate(now);
        }
        otpVerificationRepository.saveAll(pending);
        return pending;
    }

    /**
     * Resumes the paused executions of superseded records, so they end instead of timing
     * out into reassignment. A record carrying the same token as the new one is a retry of
     * this same step, so there is no other execution to resume.
     */
    private void resumeSuperseded(List<OtpVerification> superseded, String newTaskToken) {
        List<String> tokens = new ArrayList<>();
        for (OtpVerification record : superseded) {
            String token = record.getTaskToken();
            if (StringUtils.hasText(token) && !token.equals(newTaskToken) && !tokens.contains(token)) {
                tokens.add(token);
            }
        }
        for (String token : tokens) {
            try {
                workflowCallbackService.sendSuccess(token,
                        Map.of(SUPERSEDED_RESULT_KEY, OtpVerificationState.SUPERSEDED.name()));
            } catch (RuntimeException e) {
                // The old execution will time out on its own; that must not block new codes.
                log.warn("Could not resume a superseded OTP execution", e);
            }
        }
    }

    /**
     * Sends a party their code on every channel they have. Succeeds if at least one
     * channel delivered; throws if all failed.
     */
    private void deliver(OtpParty party, UserContactRow contact, String requestId,
                         String code, ZonedDateTime expiresAt) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(NotificationEventType.OTP_ISSUED)
                .requestId(requestId)
                .subject("Your Saayam verification code")
                .body(messageBody(party, code, expiresAt))
                .createdAt(ZonedDateTime.now(clock))
                .build();

        int delivered = 0;
        RuntimeException lastFailure = null;

        if (StringUtils.hasText(contact.getPhoneNumber())) {
            try {
                snsNotificationService.sendSms(event, new NotificationRecipient(
                        contact.getUserId(), NotificationChannel.SMS, contact.getPhoneNumber()));
                delivered++;
            } catch (RuntimeException e) {
                lastFailure = e;
                log.warn("OTP SMS delivery failed for {} {} on request {}", party, contact.getUserId(), requestId);
            }
        }

        if (StringUtils.hasText(contact.getEmailAddress())) {
            try {
                emailNotificationService.sendEmail(event, new NotificationRecipient(
                        contact.getUserId(), NotificationChannel.EMAIL, contact.getEmailAddress()));
                delivered++;
            } catch (RuntimeException e) {
                lastFailure = e;
                log.warn("OTP email delivery failed for {} {} on request {}", party, contact.getUserId(), requestId);
            }
        }

        if (delivered == 0) {
            throw new OtpDeliveryFailedException(party, contact.getUserId(), lastFailure);
        }
    }

    static String messageBody(OtpParty party, String code, ZonedDateTime expiresAt) {
        String otherParty = party == OtpParty.REQUESTER ? "your volunteer" : "the person you are helping";
        return "Your Saayam verification code is " + code + ".\n\n"
                + "When you meet " + otherParty + ", read this code to them, then enter the code "
                + "they read to you in the Saayam app. Do not share it with anyone else.\n\n"
                + "This code expires on " + EXPIRY_FORMAT.format(expiresAt.withZoneSameInstant(ZoneOffset.UTC)) + ".";
    }
}
