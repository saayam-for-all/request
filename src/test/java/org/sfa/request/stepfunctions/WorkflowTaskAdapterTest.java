package org.sfa.request.stepfunctions;

import org.junit.jupiter.api.Test;
import org.sfa.request.dto.notification.NotificationChannel;
import org.sfa.request.dto.notification.NotificationRecipient;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestStatus;
import org.sfa.request.model.entity.RequestType;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.model.enums.SentimentCode;
import org.sfa.request.repository.RequestRepository;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.SentimentAssessment;
import org.sfa.request.service.api.VolunteerMatchService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowTaskAdapterTest {
    private final SentimentAnalysisService sentiment = mock(SentimentAnalysisService.class);
    private final VolunteerMatchService matching = mock(VolunteerMatchService.class);
    private final RequestRepository requests = mock(RequestRepository.class);
    private final WorkflowTaskAdapter adapter = new WorkflowTaskAdapter(sentiment, matching, requests);

    @Test
    void classifierUsesExistingServiceAndReturnsApprovedCodeOnly() {
        when(sentiment.assess(any(Request.class)))
                .thenReturn(new SentimentAssessment(SentimentCode.THREATENING, "sensitive term"));
        assertEquals(3, adapter.classify("subject", "description").code());
    }

    @Test
    void matchDeduplicatesChannelsAndOmitsAddresses() {
        RequestType type = new RequestType();
        type.setRequestTypeId(1);
        RequestStatus status = new RequestStatus();
        status.setStatus("CREATED");
        Request request = Request.builder()
                .requestType(type).requestStatus(status).requesterId("SID-1")
                .helpCategory(category()).isCalamity(false).build();
        when(requests.findById("REQ-1")).thenReturn(Optional.of(request));
        when(matching.resolveMatchedVolunteerRecipients(request)).thenReturn(List.of(
                new NotificationRecipient("VOL-1", NotificationChannel.IN_APP, null),
                new NotificationRecipient("VOL-1", NotificationChannel.EMAIL, "private@example.com"),
                new NotificationRecipient("VOL-2", NotificationChannel.IN_APP, null)));
        var result = adapter.match("REQ-1", 1, "SID-1", "CAT-1", false);
        assertTrue(result.hasCandidates());
        assertEquals(List.of("VOL-1", "VOL-2"), result.candidates());
        assertFalse(result.toString().contains("private@example.com"));
    }

    @Test
    void terminalRequestCannotBeRematched() {
        RequestType type = new RequestType();
        type.setRequestTypeId(2);
        RequestStatus status = new RequestStatus();
        status.setStatus("CANCELLED");
        Request request = Request.builder()
                .requestType(type).requestStatus(status).requesterId("SID-1")
                .helpCategory(category()).isCalamity(false).build();
        when(requests.findById("REQ-1")).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class, () -> adapter.match("REQ-1", 2, "SID-1", "CAT-1", false));
        verifyNoInteractions(matching);
    }

    @Test
    void inProgressRequestCannotBeRematched() {
        RequestType type = new RequestType();
        type.setRequestTypeId(2);
        RequestStatus status = new RequestStatus();
        status.setStatus("IN_PROGRESS");
        Request request = Request.builder().requestType(type).requestStatus(status)
                .requesterId("SID-1").helpCategory(category()).isCalamity(false).build();
        when(requests.findById("REQ-1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> adapter.match("REQ-1", 2, "SID-1", "CAT-1", false));
        verifyNoInteractions(matching);
    }

    @Test
    void changedCategoryOrRequesterCannotReuseAStaleMatchTask() {
        RequestType type = new RequestType();
        type.setRequestTypeId(2);
        RequestStatus status = new RequestStatus();
        status.setStatus("CREATED");
        Request request = Request.builder().requestType(type).requestStatus(status)
                .requesterId("SID-1").helpCategory(category()).isCalamity(false).build();
        when(requests.findById("REQ-1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> adapter.match("REQ-1", 2, "SID-OTHER", "CAT-1", false));
        assertThrows(IllegalStateException.class,
                () -> adapter.match("REQ-1", 2, "SID-1", "CAT-OTHER", false));
        verifyNoInteractions(matching);
    }

    @Test
    void calamityIsNotRoutedWithoutApprovedPolicy() {
        RequestType type = new RequestType();
        type.setRequestTypeId(1);
        RequestStatus status = new RequestStatus();
        status.setStatus("CREATED");
        Request request = Request.builder().requestType(type).requestStatus(status)
                .requesterId("SID-1").helpCategory(category()).isCalamity(true).build();
        when(requests.findById("REQ-1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> adapter.match("REQ-1", 1, "SID-1", "CAT-1", true));
        verifyNoInteractions(matching);
    }

    private static HelpCategory category() {
        HelpCategory category = new HelpCategory();
        category.setCatId("CAT-1");
        return category;
    }
}
