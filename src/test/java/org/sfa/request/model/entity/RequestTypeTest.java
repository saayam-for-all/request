package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.enums.RequestTypeEnum;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTypeTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType = new RequestType();
        requestType.setRequestTypeId(1);
        requestType.setType(RequestTypeEnum.UNSPECIFIED);
        requestType.setDescription("UNSPECIFIED type description");
        requestType.setLastUpdatedAt(lastUpdatedAtTime);
        requestType.setRequests(requestSet);

        assertThat(requestType.getRequestTypeId()).isEqualTo(1);
        assertThat(requestType.getType()).isEqualTo(RequestTypeEnum.UNSPECIFIED);
        assertThat(requestType.getDescription()).isEqualTo("UNSPECIFIED type description");
        assertThat(requestType.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestType.getRequests()).isEqualTo(requestSet);
        assertThat(requestType.getRequests()).isNotNull();
        assertThat(requestType.getRequests()).isEmpty();

        String expectedToString = "RequestType(requestTypeId=1, type=UNSPECIFIED, " +
                "description=UNSPECIFIED type description, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestType.toString()).isEqualTo(expectedToString);

        System.out.println(requestType.toString().equals(expectedToString));
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType = new RequestType(1, RequestTypeEnum.UNSPECIFIED,
                "UNSPECIFIED type description", lastUpdatedAtTime, requestSet);

        assertThat(requestType.getRequestTypeId()).isEqualTo(1);
        assertThat(requestType.getType()).isEqualTo(RequestTypeEnum.UNSPECIFIED);
        assertThat(requestType.getDescription()).isEqualTo("UNSPECIFIED type description");
        assertThat(requestType.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestType.getRequests()).isEqualTo(requestSet);
        assertThat(requestType.getRequests()).isNotNull();
        assertThat(requestType.getRequests()).isEmpty();

        String expectedToString = "RequestType(requestTypeId=1, type=UNSPECIFIED, " +
                "description=UNSPECIFIED type description, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestType.toString()).isEqualTo(expectedToString);

        System.out.println(requestType.toString().equals(expectedToString));
    }

    @Test
    void testEquals() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType(RequestTypeEnum.UNSPECIFIED);
        requestType1.setDescription("UNSPECIFIED type description");
        requestType1.setLastUpdatedAt(lastUpdatedAtTime);
        requestType1.setRequests(requestSet);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType(RequestTypeEnum.UNSPECIFIED);
        requestType2.setDescription("UNSPECIFIED type description");
        requestType2.setLastUpdatedAt(lastUpdatedAtTime);
        requestType2.setRequests(requestSet);

        RequestType requestType3 = new RequestType();
        requestType3.setRequestTypeId(3);
        requestType3.setType(RequestTypeEnum.REMOTE);
        requestType3.setDescription("Other type description");
        requestType3.setLastUpdatedAt(lastUpdatedAtTime);
        requestType3.setRequests(requestSet);

        assertThat(requestType1).isEqualTo(requestType2);
        assertThat(requestType2).isEqualTo(requestType1);

        assertThat(requestType1).isNotEqualTo(requestType3);
        assertThat(requestType2).isNotEqualTo(requestType3);
        assertThat(requestType1).isNotEqualTo("Different type to RequestType");
        assertThat(requestType1).isNotEqualTo(null);
    }

    @Test
    void testEquals2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType(null);
        requestType1.setDescription(null);
        requestType1.setLastUpdatedAt(null);
        requestType1.setRequests(null);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType(null);
        requestType2.setDescription(null);
        requestType2.setLastUpdatedAt(null);
        requestType2.setRequests(null);

        assertThat(requestType1).isEqualTo(requestType2);

        RequestType requestType3 = new RequestType();
        requestType3.setRequestTypeId(2);
        assertThat(requestType1).isNotEqualTo(requestType3);
    }

    @Test
    void testEqualsHashCode() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType = new RequestType();
        requestType.setRequestTypeId(1);
        requestType.setType(RequestTypeEnum.UNSPECIFIED);
        requestType.setDescription("UNSPECIFIED type description");
        requestType.setLastUpdatedAt(lastUpdatedAtTime);
        requestType.setRequests(requestSet);

        assertThat(requestType).isEqualTo(requestType);

        int expectedHashCodeResult = requestType.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestType.hashCode());
    }

    @Test
    void testEqualsHashCode2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestType requestType1 = new RequestType();
        requestType1.setRequestTypeId(1);
        requestType1.setType(RequestTypeEnum.UNSPECIFIED);
        requestType1.setDescription("UNSPECIFIED type description");
        requestType1.setLastUpdatedAt(lastUpdatedAtTime);
        requestType1.setRequests(requestSet);

        RequestType requestType2 = new RequestType();
        requestType2.setRequestTypeId(1);
        requestType2.setType(RequestTypeEnum.UNSPECIFIED);
        requestType2.setDescription("UNSPECIFIED type description");
        requestType2.setLastUpdatedAt(lastUpdatedAtTime);
        requestType2.setRequests(requestSet);

        assertThat(requestType1).isEqualTo(requestType2);
        int expectedHashCodeResult = requestType1.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestType2.hashCode());
    }
}
