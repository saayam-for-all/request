package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTypeTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType = new RequestType();
        requestType.setRequestTypeId(1);
        requestType.setType("UNSPECIFIED");
        requestType.setDescription("UNSPECIFIED type description");
        requestType.setLastUpdatedAt(now);
        requestType.setRequests(requestSet);

        assertThat(requestType.getRequestTypeId()).isEqualTo(1);
        assertThat(requestType.getType()).isEqualTo("UNSPECIFIED");
        assertThat(requestType.getDescription()).isEqualTo("UNSPECIFIED type description");
        assertThat(requestType.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestType.getRequests()).isNotNull();
        assertThat(requestType.getRequests()).isEmpty();
    }

    @Test
    void testAllArgsConstructor() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType = new RequestType(1, "UNSPECIFIED",
                "UNSPECIFIED type description", now, requestSet);

        assertThat(requestType.getRequestTypeId()).isEqualTo(1);
        assertThat(requestType.getType()).isEqualTo("UNSPECIFIED");
        assertThat(requestType.getDescription()).isEqualTo("UNSPECIFIED type description");
        assertThat(requestType.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestType.getRequests()).isNotNull();
    }

    @Test
    void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType("UNSPECIFIED");
        requestType1.setDescription("UNSPECIFIED type description");
        requestType1.setLastUpdatedAt(now);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType("UNSPECIFIED");
        requestType2.setDescription("UNSPECIFIED type description");
        requestType2.setLastUpdatedAt(now);

        RequestType requestType3 = new RequestType();
        requestType3.setRequestTypeId(3);
        requestType3.setType("REMOTE");
        requestType3.setDescription("Other type description");
        requestType3.setLastUpdatedAt(now);

        assertThat(requestType1).isEqualTo(requestType2);
        assertThat(requestType1).isNotEqualTo(requestType3);
        assertThat(requestType1).isNotEqualTo(null);
        assertThat(requestType1).isNotEqualTo("Different type");
    }

    @Test
    void testEquals_nullFields() {
        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType(null);
        requestType1.setDescription(null);
        requestType1.setLastUpdatedAt(null);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType(null);
        requestType2.setDescription(null);
        requestType2.setLastUpdatedAt(null);

        assertThat(requestType1).isEqualTo(requestType2);

        RequestType requestType3 = new RequestType();
        requestType3.setRequestTypeId(2);
        assertThat(requestType1).isNotEqualTo(requestType3);
    }

    @Test
    void testHashCode() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType("UNSPECIFIED");
        requestType1.setLastUpdatedAt(now);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType("UNSPECIFIED");
        requestType2.setLastUpdatedAt(now);

        assertThat(requestType1.hashCode()).isEqualTo(requestType2.hashCode());
    }
}