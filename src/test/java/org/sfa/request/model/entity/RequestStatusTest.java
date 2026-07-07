package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestStatusTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestStatusId(1);
        requestStatus.setStatus("IN_PROGRESS");
        requestStatus.setDescription("This is a test status description");
        requestStatus.setLastUpdatedAt(now);
        requestStatus.setRequests(requestSet);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(1);
        assertThat(requestStatus.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(requestStatus.getDescription()).isEqualTo("This is a test status description");
        assertThat(requestStatus.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestStatus.getRequests()).isNotNull();
        assertThat(requestStatus.getRequests()).isEmpty();
    }

    @Test
    void testAllArgsConstructor() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus = new RequestStatus(1, "IN_PROGRESS",
                "This is a test status description", now, requestSet);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(1);
        assertThat(requestStatus.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(requestStatus.getDescription()).isEqualTo("This is a test status description");
        assertThat(requestStatus.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestStatus.getRequests()).isNotNull();
    }

    @Test
    void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus("IN_PROGRESS");
        requestStatus1.setDescription("This is a test status description");
        requestStatus1.setLastUpdatedAt(now);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus("IN_PROGRESS");
        requestStatus2.setDescription("This is a test status description");
        requestStatus2.setLastUpdatedAt(now);

        RequestStatus requestStatus3 = new RequestStatus();
        requestStatus3.setRequestStatusId(2);
        requestStatus3.setStatus("COMPLETED");
        requestStatus3.setDescription("Different description");
        requestStatus3.setLastUpdatedAt(now);

        assertThat(requestStatus1).isEqualTo(requestStatus2);
        assertThat(requestStatus1).isNotEqualTo(requestStatus3);
        assertThat(requestStatus1).isNotEqualTo(null);
        assertThat(requestStatus1).isNotEqualTo("Different type");
    }

    @Test
    void testEquals_nullFields() {
        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus(null);
        requestStatus1.setDescription(null);
        requestStatus1.setLastUpdatedAt(null);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus(null);
        requestStatus2.setDescription(null);
        requestStatus2.setLastUpdatedAt(null);

        assertThat(requestStatus1).isEqualTo(requestStatus2);

        RequestStatus requestStatus3 = new RequestStatus();
        requestStatus3.setRequestStatusId(2);
        assertThat(requestStatus1).isNotEqualTo(requestStatus3);
    }

    @Test
    void testHashCode() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus("IN_PROGRESS");
        requestStatus1.setLastUpdatedAt(now);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus("IN_PROGRESS");
        requestStatus2.setLastUpdatedAt(now);

        assertThat(requestStatus1.hashCode()).isEqualTo(requestStatus2.hashCode());
    }

    @Test
    void testGetId() {
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestStatusId(5);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(requestStatus.getId());
    }
}