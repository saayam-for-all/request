package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime submittedAt = ZonedDateTime.now();
        ZonedDateTime servicedAt = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAt = ZonedDateTime.now();

        Request request = new Request();
        request.setRequestId("REQ12345");
        request.setRequesterId("USR123");
        request.setRequestStatus(new RequestStatus());
        request.setRequestPriority(new RequestPriority());
        request.setRequestType(new RequestType());
        request.setHelpCategory(new HelpCategory());
        request.setRequestFor(new RequestFor());
        request.setRequestLocation("New York");
        request.setRequestSubject("Need help");
        request.setRequestDescription("This is a test request.");
        request.setAudioRequestDescription("Test audio description");
        request.setIsCalamity(false);
        request.setRequestDocumentLink("http://doc.link");
        request.setSubmittedAt(submittedAt);
        request.setServicedAt(servicedAt);
        request.setLastUpdatedAt(lastUpdatedAt);

        assertThat(request.getRequestId()).isEqualTo("REQ12345");
        assertThat(request.getRequesterId()).isEqualTo("USR123");
        assertThat(request.getRequestStatus()).isNotNull();
        assertThat(request.getRequestPriority()).isNotNull();
        assertThat(request.getRequestType()).isNotNull();
        assertThat(request.getHelpCategory()).isNotNull();
        assertThat(request.getRequestFor()).isNotNull();
        assertThat(request.getRequestLocation()).isEqualTo("New York");
        assertThat(request.getRequestSubject()).isEqualTo("Need help");
        assertThat(request.getRequestDescription()).isEqualTo("This is a test request.");
        assertThat(request.getAudioRequestDescription()).isEqualTo("Test audio description");
        assertThat(request.getIsCalamity()).isEqualTo(false);
        assertThat(request.getRequestDocumentLink()).isEqualTo("http://doc.link");
        assertThat(request.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(request.getServicedAt()).isEqualTo(servicedAt);
        assertThat(request.getLastUpdatedAt()).isEqualTo(lastUpdatedAt);
    }

    @Test
    void testRequestBuilder() {
        ZonedDateTime submittedAt = ZonedDateTime.now();
        ZonedDateTime servicedAt = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAt = ZonedDateTime.now();

        Request request = Request.builder()
                .requestId("REQ12345")
                .requesterId("USR123")
                .requestStatus(new RequestStatus())
                .requestPriority(new RequestPriority())
                .requestType(new RequestType())
                .helpCategory(new HelpCategory())
                .requestFor(new RequestFor())
                .requestLocation("New York")
                .requestSubject("Need help")
                .requestDescription("This is a test request.")
                .audioRequestDescription("Test audio description")
                .isCalamity(false)
                .requestDocumentLink("http://doc.link")
                .submittedAt(submittedAt)
                .servicedAt(servicedAt)
                .lastUpdatedAt(lastUpdatedAt)
                .build();

        assertThat(request.getRequestId()).isEqualTo("REQ12345");
        assertThat(request.getRequesterId()).isEqualTo("USR123");
        assertThat(request.getRequestLocation()).isEqualTo("New York");
        assertThat(request.getRequestSubject()).isEqualTo("Need help");
        assertThat(request.getRequestDescription()).isEqualTo("This is a test request.");
        assertThat(request.getAudioRequestDescription()).isEqualTo("Test audio description");
        assertThat(request.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(request.getServicedAt()).isEqualTo(servicedAt);
        assertThat(request.getLastUpdatedAt()).isEqualTo(lastUpdatedAt);
    }

    @Test
    void testEqualsAndHashCode() {
        Request request1 = new Request();
        request1.setRequestId("REQ12345");

        // no @EqualsAndHashCode — uses reference equality
        assertThat(request1).isEqualTo(request1);
        assertThat(request1.hashCode()).isEqualTo(request1.hashCode());

        Request request2 = new Request();
        request2.setRequestId("REQ12345");
        assertThat(request1).isNotEqualTo(request2);
    }
}