package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.enums.RequestStatusEnum;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestStatusTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestStatusId(1);
        requestStatus.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus.setDescription("This is a test status description");
        requestStatus.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus.setRequests(requestSet);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(1);
        assertThat(requestStatus.getStatus()).isEqualTo(RequestStatusEnum.IN_PROGRESS);
        assertThat(requestStatus.getDescription()).isEqualTo("This is a test status description");
        assertThat(requestStatus.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestStatus.getRequests()).isEqualTo(requestSet);
        assertThat(requestStatus.getRequests()).isNotNull();
        assertThat(requestStatus.getRequests()).isEmpty();

        String expectedToString = "RequestStatus(requestStatusId=1, status=IN_PROGRESS, description=This is a test status description, " +
                "lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestStatus.toString()).isEqualTo(expectedToString);

        System.out.println(requestStatus.toString().equals(expectedToString));
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus = new RequestStatus(1, RequestStatusEnum.IN_PROGRESS,
                "This is a test status description", lastUpdatedAtTime, requestSet);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(1);
        assertThat(requestStatus.getStatus()).isEqualTo(RequestStatusEnum.IN_PROGRESS);
        assertThat(requestStatus.getDescription()).isEqualTo("This is a test status description");
        assertThat(requestStatus.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestStatus.getRequests()).isEqualTo(requestSet);
        assertThat(requestStatus.getRequests()).isNotNull();
        assertThat(requestStatus.getRequests()).isEmpty();

        String expectedToString = "RequestStatus(requestStatusId=1, status=IN_PROGRESS, description=This is a test status description, " +
                "lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestStatus.toString()).isEqualTo(expectedToString);

        System.out.println(requestStatus.toString().equals(expectedToString));
    }

    @Test
    void testEquals() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus1.setDescription("This is a test status description");
        requestStatus1.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus1.setRequests(requestSet);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus2.setDescription("This is a test status description");
        requestStatus2.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus2.setRequests(requestSet);

        RequestStatus requestStatus3 = new RequestStatus();
        requestStatus3.setRequestStatusId(2);
        requestStatus3.setStatus(RequestStatusEnum.COMPLETED);
        requestStatus3.setDescription("Different status description");
        requestStatus3.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus3.setRequests(requestSet);

        assertThat(requestStatus1).isEqualTo(requestStatus2);
        assertThat(requestStatus2).isEqualTo(requestStatus1);

        assertThat(requestStatus1).isNotEqualTo(requestStatus3);
        assertThat(requestStatus2).isNotEqualTo(requestStatus3);
        assertThat(requestStatus1).isNotEqualTo("Different type to RequestStatus");
        assertThat(requestStatus1).isNotEqualTo(null);
    }

    @Test
    void testEquals2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus(null);
        requestStatus1.setDescription(null);
        requestStatus1.setLastUpdatedAt(null);
        requestStatus1.setRequests(null);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus(null);
        requestStatus2.setDescription(null);
        requestStatus2.setLastUpdatedAt(null);
        requestStatus2.setRequests(null);

        assertThat(requestStatus1).isEqualTo(requestStatus2);

        RequestStatus requestStatus3 = new RequestStatus();
        requestStatus3.setRequestStatusId(2);
        assertThat(requestStatus1).isNotEqualTo(requestStatus3);
    }

    @Test
    void testEqualsHashCode() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestStatusId(1);
        requestStatus.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus.setDescription("This is a test status description");
        requestStatus.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus.setRequests(requestSet);

        assertThat(requestStatus).isEqualTo(requestStatus);

        int expectedHashCodeResult = requestStatus.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestStatus.hashCode());
    }

    @Test
    void testEqualsHashCode2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestStatus requestStatus1 = new RequestStatus();
        requestStatus1.setRequestStatusId(1);
        requestStatus1.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus1.setDescription("This is a test status description");
        requestStatus1.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus1.setRequests(requestSet);

        RequestStatus requestStatus2 = new RequestStatus();
        requestStatus2.setRequestStatusId(1);
        requestStatus2.setStatus(RequestStatusEnum.IN_PROGRESS);
        requestStatus2.setDescription("This is a test status description");
        requestStatus2.setLastUpdatedAt(lastUpdatedAtTime);
        requestStatus2.setRequests(requestSet);

        assertThat(requestStatus1).isEqualTo(requestStatus2);
        int expectedHashCodeResult = requestStatus1.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestStatus2.hashCode());
    }

    @Test
    void testGetId(){
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestStatusId(5);

        assertThat(requestStatus.getRequestStatusId()).isEqualTo(requestStatus.getId());
    }
}
