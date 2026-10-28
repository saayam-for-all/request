package org.sfa.request.stepfunctions;

import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.RequestTypeEnum;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.VolunteerMatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Existing-service implementation of two workflow task contracts; no side effects. */
@Service
public class WorkflowTaskAdapter {
    private static final Set<String> TERMINAL = Set.of("CANCELLED", "DELETED", "RESOLVED");
    private final SentimentAnalysisService sentiment;
    private final VolunteerMatchService matching;
    private final RequestRepository requests;

    public WorkflowTaskAdapter(SentimentAnalysisService sentiment, VolunteerMatchService matching,
                               RequestRepository requests) {
        this.sentiment = sentiment;
        this.matching = matching;
        this.requests = requests;
    }

    public Classification classify(String subject, String description) {
        if (subject == null || subject.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Subject and description are required");
        }
        Request content = Request.builder().requestSubject(subject).requestDescription(description).build();
        var assessment = sentiment.assess(content);
        if (assessment == null || assessment.code() == null) {
            throw new IllegalStateException("Classifier returned no approved code");
        }
        return new Classification(assessment.code().getCode());
    }

    @Transactional(readOnly = true)
    public MatchResult match(String requestId, int expectedTypeId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID is required");
        }
        if (expectedTypeId != RequestTypeEnum.IN_PERSON.getId()
                && expectedTypeId != RequestTypeEnum.REMOTE.getId()) {
            throw new IllegalArgumentException("Unsupported request type");
        }
        Request request = requests.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown request ID"));
        if (request.getRequestStatus() == null || request.getRequestStatus().getStatus() == null
                || TERMINAL.contains(request.getRequestStatus().getStatus().toUpperCase(Locale.ROOT))) {
            throw new IllegalStateException("Request is not active");
        }
        if (request.getRequestType() == null
                || request.getRequestType().getRequestTypeId() == null
                || request.getRequestType().getRequestTypeId() != expectedTypeId) {
            throw new IllegalStateException("Request type changed after submission");
        }
        // Existing matching returns one recipient per delivery channel. Keep only
        // distinct volunteer IDs in the SFN execution history; never email addresses.
        List<String> candidateIds = matching.resolveMatchedVolunteerRecipients(request).stream()
                .map(NotificationRecipient::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return new MatchResult(!candidateIds.isEmpty(), candidateIds);
    }

    public record Classification(int code) { }
    public record MatchResult(boolean hasCandidates, List<String> candidates) { }
}
