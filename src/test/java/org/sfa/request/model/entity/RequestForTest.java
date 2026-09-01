package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.enums.RequestForEnum;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestForTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(1);
        requestFor.setFor(RequestForEnum.UNSPECIFIED);
        requestFor.setDescription("Technical support request");
        requestFor.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor.setRequests(requestSet);

        assertThat(requestFor.getRequestForId()).isEqualTo(1);
        assertThat(requestFor.getFor()).isEqualTo(RequestForEnum.UNSPECIFIED);
        assertThat(requestFor.getDescription()).isEqualTo("Technical support request");
        assertThat(requestFor.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestFor.getRequests()).isEqualTo(requestSet);
        assertThat(requestFor.getRequests()).isNotNull();
        assertThat(requestFor.getRequests()).isEmpty();

        String expectedToString = "RequestFor(requestForId=1, For=UNSPECIFIED, " +
                "description=Technical support request, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestFor.toString()).isEqualTo(expectedToString);
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor = new RequestFor(1, RequestForEnum.UNSPECIFIED,
                "Technical support request", lastUpdatedAtTime, requestSet);

        assertThat(requestFor.getRequestForId()).isEqualTo(1);
        assertThat(requestFor.getFor()).isEqualTo(RequestForEnum.UNSPECIFIED);
        assertThat(requestFor.getDescription()).isEqualTo("Technical support request");
        assertThat(requestFor.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
        assertThat(requestFor.getRequests()).isEqualTo(requestSet);
        assertThat(requestFor.getRequests()).isNotNull();
        assertThat(requestFor.getRequests()).isEmpty();

        String expectedToString = "RequestFor(requestForId=1, For=UNSPECIFIED, " +
                "description=Technical support request, lastUpdatedAt=" + lastUpdatedAtTime + ", requests=[])";
        assertThat(requestFor.toString()).isEqualTo(expectedToString);
    }

    @Test
    void testEquals() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor1 = new RequestFor();
        requestFor1.setRequestForId(1);
        requestFor1.setFor(RequestForEnum.UNSPECIFIED);
        requestFor1.setDescription("Technical support request");
        requestFor1.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor1.setRequests(requestSet);

        RequestFor requestFor2 = new RequestFor();
        requestFor2.setRequestForId(1);
        requestFor2.setFor(RequestForEnum.UNSPECIFIED);
        requestFor2.setDescription("Technical support request");
        requestFor2.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor2.setRequests(requestSet);

        RequestFor requestFor3 = new RequestFor();
        requestFor3.setRequestForId(3);
        requestFor3.setFor(RequestForEnum.OTHER);
        requestFor3.setDescription("General inquiry request");
        requestFor3.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor3.setRequests(requestSet);

        assertThat(requestFor1).isEqualTo(requestFor2);
        assertThat(requestFor2).isEqualTo(requestFor1);

        assertThat(requestFor1).isNotEqualTo(requestFor3);
        assertThat(requestFor2).isNotEqualTo(requestFor3);
        assertThat(requestFor1).isNotEqualTo("Different type to RequestFor");
        assertThat(requestFor1).isNotEqualTo(null);
    }

    @Test
    void testEquals2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor1 = new RequestFor();
        requestFor1.setRequestForId(1);
        requestFor1.setFor(null);
        requestFor1.setDescription(null);
        requestFor1.setLastUpdatedAt(null);
        requestFor1.setRequests(null);

        RequestFor requestFor2 = new RequestFor();
        requestFor2.setRequestForId(1);
        requestFor2.setFor(null);
        requestFor2.setDescription(null);
        requestFor2.setLastUpdatedAt(null);
        requestFor2.setRequests(null);

        assertThat(requestFor1).isEqualTo(requestFor2);

        RequestFor requestFor3 = new RequestFor();
        requestFor3.setRequestForId(2);
        assertThat(requestFor1).isNotEqualTo(requestFor3);
    }

    @Test
    void testHashCode() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(1);
        requestFor.setFor(RequestForEnum.UNSPECIFIED);
        requestFor.setDescription("Technical support request");
        requestFor.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor.setRequests(requestSet);

        assertThat(requestFor).isEqualTo(requestFor);

        int expectedHashCodeResult = requestFor.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestFor.hashCode());
    }

    @Test
    void testHashCode2() {
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor1 = new RequestFor();
        requestFor1.setRequestForId(1);
        requestFor1.setFor(RequestForEnum.UNSPECIFIED);
        requestFor1.setDescription("Technical support request");
        requestFor1.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor1.setRequests(requestSet);

        RequestFor requestFor2 = new RequestFor();
        requestFor2.setRequestForId(1);
        requestFor2.setFor(RequestForEnum.UNSPECIFIED);
        requestFor2.setDescription("Technical support request");
        requestFor2.setLastUpdatedAt(lastUpdatedAtTime);
        requestFor2.setRequests(requestSet);

        assertThat(requestFor1).isEqualTo(requestFor2);
        int expectedHashCodeResult = requestFor1.hashCode();
        assertThat(expectedHashCodeResult).isEqualTo(requestFor2.hashCode());
    }
}
