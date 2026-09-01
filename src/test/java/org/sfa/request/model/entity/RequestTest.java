package org.sfa.request.model.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTest {

    @Test
    void testGettersAndSetters() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request = new Request();
        request.setRequestId("REQ12345");
        request.setRequesterId("USR123");
        request.setRequestStatus(new RequestStatus());
        request.setRequestPriority(new RequestPriority());
        request.setRequestType(new RequestType());
        request.setRequestCategory(new RequestCategory());
        request.setRequestFor(new RequestFor());
        request.setCity("New York");
        request.setZipCode("10001");
        request.setRequestDescription("This is a test request.");
        request.setAudioRequestDescription("Test audio description");
        request.setSubmittedAt(submittedAtTime);
        request.setLeadVolunteerUserId(101);
        request.setServicedAt(servicedAtTime);
        request.setLastUpdatedAt(lastUpdatedAtTime);

        assertThat(request.getRequestId()).isEqualTo("REQ12345");
        assertThat(request.getRequesterId()).isEqualTo("USR123");
        assertThat(request.getRequestStatus()).isNotNull();
        assertThat(request.getRequestPriority()).isNotNull();
        assertThat(request.getRequestType()).isNotNull();
        assertThat(request.getRequestCategory()).isNotNull();
        assertThat(request.getRequestFor()).isNotNull();
        assertThat(request.getCity()).isEqualTo("New York");
        assertThat(request.getZipCode()).isEqualTo("10001");
        assertThat(request.getRequestDescription()).isEqualTo("This is a test request.");
        assertThat(request.getAudioRequestDescription()).isEqualTo("Test audio description");
        assertThat(request.getSubmittedAt()).isEqualTo(submittedAtTime);
        assertThat(request.getLeadVolunteerUserId()).isEqualTo(101);
        assertThat(request.getServicedAt()).isEqualTo(servicedAtTime);
        assertThat(request.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);

        String expectedToString = "Request(requestId=REQ12345, requesterId=USR123, " +
                "requestStatus=" + new RequestStatus() + ", requestPriority=" + new RequestPriority() +
                ", requestType=" + new RequestType() + ", requestCategory=" + new RequestCategory() +
                ", requestFor=" + new RequestFor() + ", city=New York, zipCode=10001, requestDescription=This is a test request., " +
                "audioRequestDescription=Test audio description, submittedAt=" + submittedAtTime +
                ", leadVolunteerUserId=101, servicedAt=" + servicedAtTime + ", lastUpdatedAt=" + lastUpdatedAtTime + ")";
        System.out.println(request.toString());
        assertThat(request.toString()).isEqualTo(expectedToString);
    }

    @Test
    void testGettersAndSetters2() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request = new Request("REQ12345", "USR123", new RequestStatus(), new RequestPriority(),
                new RequestType(), new RequestCategory(), new RequestFor(), "New York", "10001",
                "This is a test request.", "Test audio description", submittedAtTime, 101, servicedAtTime,
                lastUpdatedAtTime);

        assertThat(request.getRequestId()).isEqualTo("REQ12345");
        assertThat(request.getRequesterId()).isEqualTo("USR123");
        assertThat(request.getRequestStatus()).isNotNull();
        assertThat(request.getRequestPriority()).isNotNull();
        assertThat(request.getRequestType()).isNotNull();
        assertThat(request.getRequestCategory()).isNotNull();
        assertThat(request.getRequestFor()).isNotNull();
        assertThat(request.getCity()).isEqualTo("New York");
        assertThat(request.getZipCode()).isEqualTo("10001");
        assertThat(request.getRequestDescription()).isEqualTo("This is a test request.");
        assertThat(request.getAudioRequestDescription()).isEqualTo("Test audio description");
        assertThat(request.getSubmittedAt()).isEqualTo(submittedAtTime);
        assertThat(request.getLeadVolunteerUserId()).isEqualTo(101);
        assertThat(request.getServicedAt()).isEqualTo(servicedAtTime);
        assertThat(request.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);

        String expectedToString = "Request(requestId=REQ12345, requesterId=USR123, " +
                "requestStatus=" + new RequestStatus() + ", requestPriority=" + new RequestPriority() +
                ", requestType=" + new RequestType() + ", requestCategory=" + new RequestCategory() +
                ", requestFor=" + new RequestFor() + ", city=New York, zipCode=10001, requestDescription=This is a test request., " +
                "audioRequestDescription=Test audio description, submittedAt=" + submittedAtTime +
                ", leadVolunteerUserId=101, servicedAt=" + servicedAtTime + ", lastUpdatedAt=" + lastUpdatedAtTime + ")";
        assertThat(request.toString()).isEqualTo(expectedToString);
    }

    @Test
    void testRequestBuilder() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request = Request.builder()
                .requestId("REQ12345")
                .requesterId("USR123")
                .requestStatus(new RequestStatus())
                .requestPriority(new RequestPriority())
                .requestType(new RequestType())
                .requestCategory(new RequestCategory())
                .requestFor(new RequestFor())
                .city("New York")
                .zipCode("10001")
                .requestDescription("This is a test request.")
                .audioRequestDescription("Test audio description")
                .submittedAt(submittedAtTime)
                .leadVolunteerUserId(101)
                .servicedAt(servicedAtTime)
                .lastUpdatedAt(lastUpdatedAtTime)
                .build();

        assertThat(request.getRequestId()).isEqualTo("REQ12345");
        assertThat(request.getRequesterId()).isEqualTo("USR123");
        assertThat(request.getCity()).isEqualTo("New York");
        assertThat(request.getZipCode()).isEqualTo("10001");
        assertThat(request.getRequestDescription()).isEqualTo("This is a test request.");
        assertThat(request.getAudioRequestDescription()).isEqualTo("Test audio description");
        assertThat(request.getSubmittedAt()).isEqualTo(submittedAtTime);
        assertThat(request.getServicedAt()).isEqualTo(servicedAtTime);
        assertThat(request.getLastUpdatedAt()).isEqualTo(lastUpdatedAtTime);
    }

    @Test
    void testEquals() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request1 = new Request();
        request1.setRequestId("REQ12345");
        request1.setRequesterId("USR123");
        request1.setRequestStatus(new RequestStatus());
        request1.setRequestPriority(new RequestPriority());
        request1.setRequestType(new RequestType());
        request1.setRequestCategory(new RequestCategory());
        request1.setRequestFor(new RequestFor());
        request1.setCity("New York");
        request1.setZipCode("10001");
        request1.setRequestDescription("This is a test request.");
        request1.setAudioRequestDescription("Test audio description");
        request1.setSubmittedAt(submittedAtTime);
        request1.setLeadVolunteerUserId(101);
        request1.setServicedAt(servicedAtTime);
        request1.setLastUpdatedAt(lastUpdatedAtTime);

        Request request2 = new Request();
        request2.setRequestId("REQ12345");
        request2.setRequesterId("USR123");
        request2.setRequestStatus(new RequestStatus());
        request2.setRequestPriority(new RequestPriority());
        request2.setRequestType(new RequestType());
        request2.setRequestCategory(new RequestCategory());
        request2.setRequestFor(new RequestFor());
        request2.setCity("New York");
        request2.setZipCode("10001");
        request2.setRequestDescription("This is a test request.");
        request2.setAudioRequestDescription("Test audio description");
        request2.setSubmittedAt(submittedAtTime);
        request2.setLeadVolunteerUserId(101);
        request2.setServicedAt(servicedAtTime);
        request2.setLastUpdatedAt(lastUpdatedAtTime);

        Request request3 = new Request();
        request3.setRequestId("REQ54321");
        request3.setRequesterId("USR543");
        request3.setRequestStatus(new RequestStatus());
        request3.setRequestPriority(new RequestPriority());
        request3.setRequestType(new RequestType());
        request3.setRequestCategory(new RequestCategory());
        request3.setRequestFor(new RequestFor());
        request3.setCity("Los Angeles");
        request3.setZipCode("90001");
        request3.setRequestDescription("Different request description.");
        request3.setAudioRequestDescription("Different audio description");
        request3.setSubmittedAt(submittedAtTime.plusDays(1));
        request3.setLeadVolunteerUserId(202);
        request3.setServicedAt(servicedAtTime.plusDays(1));
        request3.setLastUpdatedAt(lastUpdatedAtTime.plusDays(1));

        assertThat(request1).isEqualTo(request2);
        assertThat(request2).isEqualTo(request1);

        assertThat(request1).isNotEqualTo(request3);
        assertThat(request2).isNotEqualTo(request3);

        assertThat(request1).isNotEqualTo(null);
    }

    @Test
    void testEquals2() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();

        Request request1 = new Request();
        request1.setRequestId("REQ12345");
        request1.setRequesterId("USR123");
        request1.setRequestStatus(null);
        request1.setRequestPriority(null);
        request1.setRequestType(null);
        request1.setRequestCategory(null);
        request1.setRequestFor(null);
        request1.setCity(null);
        request1.setZipCode(null);
        request1.setRequestDescription(null);
        request1.setAudioRequestDescription(null);
        request1.setSubmittedAt(submittedAtTime);
        request1.setLeadVolunteerUserId(null);
        request1.setServicedAt(null);
        request1.setLastUpdatedAt(null);

        Request request2 = new Request();
        request2.setRequestId("REQ12345");
        request2.setRequesterId("USR123");
        request2.setRequestStatus(null);
        request2.setRequestPriority(null);
        request2.setRequestType(null);
        request2.setRequestCategory(null);
        request2.setRequestFor(null);
        request2.setCity(null);
        request2.setZipCode(null);
        request2.setRequestDescription(null);
        request2.setAudioRequestDescription(null);
        request2.setSubmittedAt(submittedAtTime);
        request2.setLeadVolunteerUserId(null);
        request2.setServicedAt(null);
        request2.setLastUpdatedAt(null);

        Request request3 = new Request();
        request3.setRequestId("REQ54321");
        request3.setRequesterId(null);

        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
    }

    @Test
    void testEqualsHashCode() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request1 = new Request();
        request1.setRequestId("REQ12345");
        request1.setRequesterId("USR123");
        request1.setRequestStatus(new RequestStatus());
        request1.setRequestPriority(new RequestPriority());
        request1.setRequestType(new RequestType());
        request1.setRequestCategory(new RequestCategory());
        request1.setRequestFor(new RequestFor());
        request1.setCity("New York");
        request1.setZipCode("10001");
        request1.setRequestDescription("This is a test request.");
        request1.setAudioRequestDescription("Test audio description");
        request1.setSubmittedAt(submittedAtTime);
        request1.setLeadVolunteerUserId(101);
        request1.setServicedAt(servicedAtTime);
        request1.setLastUpdatedAt(lastUpdatedAtTime);

        assertThat(request1).isEqualTo(request1);

        int expectedHashCode = request1.hashCode();
        assertThat(expectedHashCode).isEqualTo(request1.hashCode());
    }

    @Test
    void testEqualsHashCode2() {
        ZonedDateTime submittedAtTime = ZonedDateTime.now();
        ZonedDateTime servicedAtTime = ZonedDateTime.now();
        ZonedDateTime lastUpdatedAtTime = ZonedDateTime.now();

        Request request1 = new Request();
        request1.setRequestId("REQ12345");
        request1.setRequesterId("USR123");
        request1.setRequestStatus(new RequestStatus());
        request1.setRequestPriority(new RequestPriority());
        request1.setRequestType(new RequestType());
        request1.setRequestCategory(new RequestCategory());
        request1.setRequestFor(new RequestFor());
        request1.setCity("New York");
        request1.setZipCode("10001");
        request1.setRequestDescription("This is a test request.");
        request1.setAudioRequestDescription("Test audio description");
        request1.setSubmittedAt(submittedAtTime);
        request1.setLeadVolunteerUserId(101);
        request1.setServicedAt(servicedAtTime);
        request1.setLastUpdatedAt(lastUpdatedAtTime);

        Request request2 = new Request();
        request2.setRequestId("REQ12345");
        request2.setRequesterId("USR123");
        request2.setRequestStatus(new RequestStatus());
        request2.setRequestPriority(new RequestPriority());
        request2.setRequestType(new RequestType());
        request2.setRequestCategory(new RequestCategory());
        request2.setRequestFor(new RequestFor());
        request2.setCity("New York");
        request2.setZipCode("10001");
        request2.setRequestDescription("This is a test request.");
        request2.setAudioRequestDescription("Test audio description");
        request2.setSubmittedAt(submittedAtTime);
        request2.setLeadVolunteerUserId(101);
        request2.setServicedAt(servicedAtTime);
        request2.setLastUpdatedAt(lastUpdatedAtTime);

        int expectedHashCode = request1.hashCode();
        assertThat(expectedHashCode).isEqualTo(request2.hashCode());
    }
}
