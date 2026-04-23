package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.enums.RequestPriorityEnum;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestPriorityTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setPriorityId(1);
        requestPriority.setPriority(RequestPriorityEnum.HIGH);
        requestPriority.setDescription("This is a test priority description");
        requestPriority.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority.setRequests(requestSet);

        assertThat(requestPriority.getPriorityId()).isEqualTo(1);
        assertThat(requestPriority.getPriority()).isEqualTo(RequestPriorityEnum.HIGH);
        assertThat(requestPriority.getDescription()).isEqualTo("This is a test priority description");
        assertThat(requestPriority.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestPriority.getRequests()).isEqualTo(requestSet);
        assertThat(requestPriority.getRequests()).isNotNull();
        assertThat(requestPriority.getRequests()).isEmpty();

        String expectedToString = "RequestPriority(priorityId=1, priority=HIGH, description=This is a test priority description, " +
                "lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestPriority.toString()).isEqualTo(expectedToString);

        System.out.println(requestPriority.toString().equals(expectedToString));
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority = new RequestPriority(1, RequestPriorityEnum.HIGH,
                "This is a test priority description", lastUpdatedAtTime, requestSet);

        assertThat(requestPriority.getPriorityId()).isEqualTo(1);
        assertThat(requestPriority.getPriority()).isEqualTo(RequestPriorityEnum.HIGH);
        assertThat(requestPriority.getDescription()).isEqualTo("This is a test priority description");
        assertThat(requestPriority.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestPriority.getRequests()).isEqualTo(requestSet);
        assertThat(requestPriority.getRequests()).isNotNull();
        assertThat(requestPriority.getRequests()).isEmpty();

        String expectedToString = "RequestPriority(priorityId=1, priority=HIGH, description=This is a test priority description, " +
                "lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestPriority.toString()).isEqualTo(expectedToString);

        System.out.println(requestPriority.toString().equals(expectedToString));
    }

    @Test
    void testEquals() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority(RequestPriorityEnum.HIGH);
        requestPriority1.setDescription("This is a test priority description");
        requestPriority1.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority1.setRequests(requestSet);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority(RequestPriorityEnum.HIGH);
        requestPriority2.setDescription("This is a test priority description");
        requestPriority2.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority2.setRequests(requestSet);

        RequestPriority requestPriority3 = new RequestPriority();
        requestPriority3.setPriorityId(2);
        requestPriority3.setPriority(RequestPriorityEnum.LOW);
        requestPriority3.setDescription("Different description");
        requestPriority3.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority3.setRequests(requestSet);

        assertThat(requestPriority1).isEqualTo(requestPriority2);
        assertThat(requestPriority2).isEqualTo(requestPriority1);

        assertThat(requestPriority1).isNotEqualTo(requestPriority3);
        assertThat(requestPriority2).isNotEqualTo(requestPriority3);
        assertThat(requestPriority1).isNotEqualTo("Different type to RequestPriority");
        assertThat(requestPriority1).isNotEqualTo(null);
    }

    @Test
    void testEquals2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority(null);
        requestPriority1.setDescription(null);
        requestPriority1.setLastUpdatedAt(null);
        requestPriority1.setRequests(null);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority(null);
        requestPriority2.setDescription(null);
        requestPriority2.setLastUpdatedAt(null);
        requestPriority2.setRequests(null);

        assertThat(requestPriority1).isEqualTo(requestPriority2);

        RequestPriority requestPriority3 = new RequestPriority();
        requestPriority3.setPriorityId(2);
        assertThat(requestPriority1).isNotEqualTo(requestPriority3);
    }

    @Test
    void testEqualsHashCode() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setPriorityId(1);
        requestPriority.setPriority(RequestPriorityEnum.HIGH);
        requestPriority.setDescription("This is a test priority description");
        requestPriority.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority.setRequests(requestSet);

        assertThat(requestPriority).isEqualTo(requestPriority);

        int expectedHashCodeResult = requestPriority.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestPriority.hashCode());
    }

    @Test
    void testEqualsHashCode2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority(RequestPriorityEnum.HIGH);
        requestPriority1.setDescription("This is a test priority description");
        requestPriority1.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority1.setRequests(requestSet);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority(RequestPriorityEnum.HIGH);
        requestPriority2.setDescription("This is a test priority description");
        requestPriority2.setLastUpdatedAt(lastUpdatedAtTime);
        requestPriority2.setRequests(requestSet);

        assertThat(requestPriority1).isEqualTo(requestPriority2);
        int expectedHashCodeResult = requestPriority1.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestPriority2.hashCode());
    }
}
