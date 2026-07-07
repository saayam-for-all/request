package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestForTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor = new RequestFor();
        requestFor.setRequestForId(1);
        requestFor.setRequestFor("SELF");
        requestFor.setDescription("Technical support request");
        requestFor.setLastUpdatedAt(now);
        requestFor.setRequests(requestSet);

        assertThat(requestFor.getRequestForId()).isEqualTo(1);
        assertThat(requestFor.getRequestFor()).isEqualTo("SELF");
        assertThat(requestFor.getDescription()).isEqualTo("Technical support request");
        assertThat(requestFor.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestFor.getRequests()).isNotNull();
        assertThat(requestFor.getRequests()).isEmpty();
    }

    @Test
    void testAllArgsConstructor() {
        ZonedDateTime now = ZonedDateTime.now();
        Set<Request> requestSet = new HashSet<>();

        RequestFor requestFor = new RequestFor(1, "SELF",
                "Technical support request", now, requestSet);

        assertThat(requestFor.getRequestForId()).isEqualTo(1);
        assertThat(requestFor.getRequestFor()).isEqualTo("SELF");
        assertThat(requestFor.getDescription()).isEqualTo("Technical support request");
        assertThat(requestFor.getLastUpdatedAt()).isEqualTo(now);
        assertThat(requestFor.getRequests()).isNotNull();
    }

    @Test
    void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestFor requestFor1 = new RequestFor();
        requestFor1.setRequestForId(1);
        requestFor1.setRequestFor("SELF");
        requestFor1.setDescription("Technical support request");
        requestFor1.setLastUpdatedAt(now);

        RequestFor requestFor2 = new RequestFor();
        requestFor2.setRequestForId(1);
        requestFor2.setRequestFor("SELF");
        requestFor2.setDescription("Technical support request");
        requestFor2.setLastUpdatedAt(now);

        RequestFor requestFor3 = new RequestFor();
        requestFor3.setRequestForId(3);
        requestFor3.setRequestFor("OTHER");
        requestFor3.setDescription("General inquiry");
        requestFor3.setLastUpdatedAt(now);

        assertThat(requestFor1).isEqualTo(requestFor2);
        assertThat(requestFor1).isNotEqualTo(requestFor3);
        assertThat(requestFor1).isNotEqualTo(null);
        assertThat(requestFor1).isNotEqualTo("Different type");
    }

    @Test
    void testHashCode() {
        ZonedDateTime now = ZonedDateTime.now();

        RequestFor requestFor1 = new RequestFor();
        requestFor1.setRequestForId(1);
        requestFor1.setRequestFor("SELF");
        requestFor1.setLastUpdatedAt(now);

        RequestFor requestFor2 = new RequestFor();
        requestFor2.setRequestForId(1);
        requestFor2.setRequestFor("SELF");
        requestFor2.setLastUpdatedAt(now);

        assertThat(requestFor1.hashCode()).isEqualTo(requestFor2.hashCode());
    }
}