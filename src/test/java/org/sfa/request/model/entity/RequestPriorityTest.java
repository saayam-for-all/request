package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestPriorityTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority = new RequestPriority();
        requestPriority.setPriorityId(1);
        requestPriority.setPriority("HIGH");
        requestPriority.setDescription("This is a test priority description");
        requestPriority.setLastUpdatedAt(now);
        requestPriority.setRequests(requestSet);

        assertThat(requestPriority.getPriorityId()).isEqualTo(1);
        assertThat(requestPriority.getPriority()).isEqualTo("HIGH");
        assertThat(requestPriority.getDescription()).isEqualTo("This is a test priority description");
        assertThat(requestPriority.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestPriority.getRequests()).isNotNull();
        assertThat(requestPriority.getRequests()).isEmpty();
    }

    @Test
    void testAllArgsConstructor() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestPriority requestPriority = new RequestPriority(1, "HIGH",
                "This is a test priority description", now, requestSet);

        assertThat(requestPriority.getPriorityId()).isEqualTo(1);
        assertThat(requestPriority.getPriority()).isEqualTo("HIGH");
        assertThat(requestPriority.getDescription()).isEqualTo("This is a test priority description");
        assertThat(requestPriority.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestPriority.getRequests()).isNotNull();
    }

    @Test
    void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority("HIGH");
        requestPriority1.setDescription("This is a test priority description");
        requestPriority1.setLastUpdatedAt(now);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority("HIGH");
        requestPriority2.setDescription("This is a test priority description");
        requestPriority2.setLastUpdatedAt(now);

        RequestPriority requestPriority3 = new RequestPriority();
        requestPriority3.setPriorityId(2);
        requestPriority3.setPriority("LOW");
        requestPriority3.setDescription("Different description");
        requestPriority3.setLastUpdatedAt(now);

        assertThat(requestPriority1).isEqualTo(requestPriority2);
        assertThat(requestPriority1).isNotEqualTo(requestPriority3);
        assertThat(requestPriority1).isNotEqualTo(null);
        assertThat(requestPriority1).isNotEqualTo("Different type");
    }

    @Test
    void testEquals_nullFields() {
        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority(null);
        requestPriority1.setDescription(null);
        requestPriority1.setLastUpdatedAt(null);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority(null);
        requestPriority2.setDescription(null);
        requestPriority2.setLastUpdatedAt(null);

        assertThat(requestPriority1).isEqualTo(requestPriority2);

        RequestPriority requestPriority3 = new RequestPriority();
        requestPriority3.setPriorityId(2);
        assertThat(requestPriority1).isNotEqualTo(requestPriority3);
    }

    @Test
    void testHashCode() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestPriority requestPriority1 = new RequestPriority();
        requestPriority1.setPriorityId(1);
        requestPriority1.setPriority("HIGH");
        requestPriority1.setLastUpdatedAt(now);

        RequestPriority requestPriority2 = new RequestPriority();
        requestPriority2.setPriorityId(1);
        requestPriority2.setPriority("HIGH");
        requestPriority2.setLastUpdatedAt(now);

        assertThat(requestPriority1.hashCode()).isEqualTo(requestPriority2.hashCode());
    }
}