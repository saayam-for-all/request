package org.sfa.request.notification;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sfa.request.config.SpringContext;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.RequestDTO;
import org.sfa.request.dto.RequestForDTO;
import org.sfa.request.dto.RequestPriorityDTO;
import org.sfa.request.dto.RequestStatusDTO;
import org.sfa.request.dto.RequestTypeDTO;
import org.sfa.request.model.entity.Request;
import org.sfa.request.requesthandler.NotificationQueueHandler;
import org.sfa.request.response.SaayamResponse;
import org.sfa.request.service.api.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end demo of the volunteer notification feature against the controlled local
 * environment defined in {@code demo/docker-compose.demo.yml} (PostgreSQL + LocalStack).
 *
 * <p>Covers the full path: create request -> match volunteers by skill category ->
 * publish NotificationEvent to SQS -> consume with the real Lambda handler ->
 * fan out to SES (email) and the notifications table (in-app).
 *
 * <p>Run with: {@code demo/run-demo.sh}
 */
@SpringBootTest
@Import(DemoSqsClientConfig.class)
@ActiveProfiles("demo")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Volunteer notification, end to end")
class VolunteerNotificationE2EIT {

    private static final String BENEFICIARY = "SID-00-000-000-001";
    private static final String VOL_ALEX = "SID-00-000-000-002";   // matches, has email
    private static final String VOL_BLAIR = "SID-00-000-000-003";  // matches, has email
    private static final String VOL_CASEY = "SID-00-000-000-004";  // WRONG category
    private static final String VOL_DANA = "SID-00-000-000-005";   // matches, NO email

    private static final String FOOD_ASSISTANCE = "1.1";

    @Autowired private RequestService requestService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private SqsClient sqsClient;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private DemoAwsSupport aws;

    private static String createdRequestId;

    @BeforeEach
    void bindLambdaSpringContext() throws Exception {
        // The Lambda handler resolves its beans through the static SpringContext holder,
        // which would otherwise boot a second Spring application. Point it at this test's
        // context so the real handler runs against the real beans.
        Field field = SpringContext.class.getDeclaredField("context");
        field.setAccessible(true);
        field.set(null, applicationContext);
    }

    @Test
    @Order(1)
    @DisplayName("1. Creating a request publishes one notification event to SQS")
    void createRequestPublishesEvent() {
        aws.purgeQueue();
        aws.clearSentEmails();
        jdbc.update("DELETE FROM virginia_dev_saayam_rdbms.notifications");

        SaayamResponse<Request> response =
                requestService.createRequest(BENEFICIARY, buildFoodAssistanceRequest(), java.util.Locale.ENGLISH);

        createdRequestId = response.getData().getRequestId();
        assertThat(createdRequestId).startsWith("REQ-");

        // The request really is in the database -- a notification is only ever sent for
        // a request that committed.
        Integer persisted = jdbc.queryForObject(
                "SELECT COUNT(*) FROM virginia_dev_saayam_rdbms.request WHERE req_id = ?",
                Integer.class, createdRequestId);
        assertThat(persisted).isEqualTo(1);

        List<Message> messages = aws.receiveAll();
        assertThat(messages)
                .as("exactly one notification event should be queued")
                .hasSize(1);
        assertThat(messages.get(0).body())
                .contains("REQUEST_CREATED")
                .contains(createdRequestId);
    }

    @Test
    @Order(2)
    @DisplayName("2. The event addresses only category-matched volunteers, never the requester")
    void eventTargetsMatchedVolunteersOnly() {
        String body = aws.peekSingleMessageBody();

        assertThat(body).contains(VOL_ALEX).contains(VOL_BLAIR).contains(VOL_DANA);
        assertThat(body)
                .as("Casey's skill is education (2.1), not food assistance (1.1)")
                .doesNotContain(VOL_CASEY);
        assertThat(body)
                .as("the requester must never be notified about their own request")
                .doesNotContain("\"userId\":\"" + BENEFICIARY + "\"");
    }

    @Test
    @Order(3)
    @DisplayName("3. The Lambda consumer fans out to email and in-app with no failures")
    void consumerDispatchesBothChannels() {
        SQSEvent event = aws.drainIntoSqsEvent();
        assertThat(event.getRecords()).hasSize(1);

        SQSBatchResponse response = new NotificationQueueHandler().handleRequest(event, null);

        assertThat(response.getBatchItemFailures())
                .as("no message should fail; a failure here would be redriven to the DLQ")
                .isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("4. SES received email only for matched volunteers who have an address")
    void emailsSentToMatchedVolunteersWithAddresses() {
        List<String> recipients = aws.sentEmailRecipients();

        assertThat(recipients)
                .containsExactlyInAnyOrder("alex.volunteer@saayam.test", "blair.volunteer@saayam.test");
        assertThat(recipients)
                .as("Casey is not category-matched")
                .doesNotContain("casey.volunteer@saayam.test");
        assertThat(recipients)
                .as("the requester is not a recipient")
                .doesNotContain("beneficiary@saayam.test");
    }

    @Test
    @Order(5)
    @DisplayName("5. In-app rows written for every matched volunteer, including the one with no email")
    void inAppNotificationsPersisted() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT n.user_id, n.message, n.status::text AS status,
                       nt.type_name, nc.channel_name
                FROM virginia_dev_saayam_rdbms.notifications n
                JOIN virginia_dev_saayam_rdbms.notification_types nt    ON nt.type_id = n.type_id
                JOIN virginia_dev_saayam_rdbms.notification_channels nc ON nc.channel_id = n.channel_id
                ORDER BY n.user_id
                """);

        assertThat(rows).hasSize(3);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.get("channel_name")).isEqualTo("IN_APP");
            assertThat(row.get("type_name")).isEqualTo("REQUEST_CREATED");
            assertThat(row.get("status")).isEqualTo("unread");
            assertThat((String) row.get("message")).contains(createdRequestId);
        });

        assertThat(rows.stream().map(r -> r.get("user_id")))
                .containsExactlyInAnyOrder(VOL_ALEX, VOL_BLAIR, VOL_DANA);
    }

    @Test
    @Order(6)
    @DisplayName("6. The volunteer service's badge query now returns an unread count")
    void volunteerBadgeCountIsVisible() {
        // Mirrors NotificationsRepository.countNewNotifications in the volunteer service:
        // notifications newer than the user's last-seen watermark.
        Integer newForAlex = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM virginia_dev_saayam_rdbms.notifications n
                LEFT JOIN virginia_dev_saayam_rdbms.user_notification_status s
                       ON s.user_id = n.user_id
                WHERE n.user_id = ?
                  AND (s.last_accessed_at IS NULL OR n.created_at > s.last_accessed_at)
                """, Integer.class, VOL_ALEX);
        assertThat(newForAlex).isEqualTo(1);

        Integer newForCasey = jdbc.queryForObject(
                "SELECT COUNT(*) FROM virginia_dev_saayam_rdbms.notifications WHERE user_id = ?",
                Integer.class, VOL_CASEY);
        assertThat(newForCasey).isZero();
    }

    private RequestDTO buildFoodAssistanceRequest() {
        RequestDTO dto = new RequestDTO();
        dto.setRequesterId(BENEFICIARY);
        dto.setRequestSubject("Need groceries delivered this week");
        dto.setRequestDescription("I am recovering from surgery and cannot get to the store.");
        dto.setRequestLocation("San Jose, CA");
        dto.setIsCalamity(false);
        dto.setSubmittedAt(ZonedDateTime.now());
        dto.setIsLeadVolunteer(0);

        RequestStatusDTO status = new RequestStatusDTO();
        status.setRequestStatusId(0);
        dto.setRequestStatus(status);

        RequestPriorityDTO priority = new RequestPriorityDTO();
        priority.setRequestPriorityId(1);
        dto.setRequestPriority(priority);

        RequestTypeDTO type = new RequestTypeDTO();
        type.setRequestTypeId(1);
        dto.setRequestType(type);

        HelpCategoryDto category = new HelpCategoryDto();
        category.setCatId(FOOD_ASSISTANCE);
        dto.setHelpCategory(category);

        RequestForDTO requestFor = new RequestForDTO();
        requestFor.setRequestForId(0);
        dto.setRequestFor(requestFor);

        return dto;
    }
}
