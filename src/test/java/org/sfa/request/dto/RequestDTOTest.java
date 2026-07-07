package org.sfa.request.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {RequestDTO.class})
@ExtendWith(SpringExtension.class)
class RequestDTOTest {

    @Autowired
    private RequestDTO requestDTO;

    @Test
    void testCanEqual() {
        assertFalse(requestDTO.canEqual("Other"));
        assertTrue(requestDTO.canEqual(requestDTO));
    }

    @Test
    void testGettersAndSetters() {
        ZonedDateTime now = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC);

        RequestDTO dto = new RequestDTO();
        dto.setRequesterId("42");
        dto.setRequestSubject("Need help");
        dto.setRequestDescription("Request Description");
        dto.setAudioRequestDescription("Audio Request Description");
        dto.setRequestLocation("MD");
        dto.setIsCalamity(false);
        dto.setRequestDocumentLink("http://doc.link");
        dto.setSubmittedAt(now);
        dto.setServicedAt(now);
        dto.setLastUpdatedAt(now);
        dto.setIsLeadVolunteer(1);
        dto.setGuestDetails(new GuestDetailsDTO());
        dto.setRequestStatus(new RequestStatusDTO());
        dto.setRequestPriority(new RequestPriorityDTO());
        dto.setRequestType(new RequestTypeDTO());
        dto.setHelpCategory(new HelpCategoryDto());
        dto.setRequestFor(new RequestForDTO());

        assertEquals("42", dto.getRequesterId());
        assertEquals("Need help", dto.getRequestSubject());
        assertEquals("Request Description", dto.getRequestDescription());
        assertEquals("Audio Request Description", dto.getAudioRequestDescription());
        assertEquals("MD", dto.getRequestLocation());
        assertFalse(dto.getIsCalamity());
        assertEquals("http://doc.link", dto.getRequestDocumentLink());
        assertEquals(now, dto.getSubmittedAt());
        assertEquals(now, dto.getServicedAt());
        assertEquals(now, dto.getLastUpdatedAt());
        assertEquals(1, dto.getIsLeadVolunteer());
        assertNotNull(dto.getGuestDetails());
        assertNotNull(dto.getRequestStatus());
        assertNotNull(dto.getRequestPriority());
        assertNotNull(dto.getRequestType());
        assertNotNull(dto.getHelpCategory());
        assertNotNull(dto.getRequestFor());
    }

    @Test
    void testAllArgsConstructor() {
        ZonedDateTime now = LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC);

        RequestDTO dto = new RequestDTO(
                "42", "Need help", "Request Description",
                "Audio Request Description", "MD", false,
                "http://doc.link", now, now, now, 1,
                new GuestDetailsDTO(), new RequestStatusDTO(),
                new RequestPriorityDTO(), new RequestTypeDTO(),
                new HelpCategoryDto(), new RequestForDTO()
        );

        assertEquals("42", dto.getRequesterId());
        assertEquals("Need help", dto.getRequestSubject());
        assertEquals("MD", dto.getRequestLocation());
        assertNotNull(dto.getHelpCategory());
        assertNotNull(dto.getRequestStatus());
    }

    @Test
    void testEquals_sameValues_returnsEqual() {
        RequestDTO dto1 = new RequestDTO();
        dto1.setRequesterId("42");
        dto1.setRequestSubject("Need help");

        RequestDTO dto2 = new RequestDTO();
        dto2.setRequesterId("42");
        dto2.setRequestSubject("Need help");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_differentValues_returnsNotEqual() {
        RequestDTO dto1 = new RequestDTO();
        dto1.setRequesterId("42");

        RequestDTO dto2 = new RequestDTO();
        dto2.setRequesterId("99");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullAndDifferentType() {
        assertNotEquals(new RequestDTO(), null);
        assertNotEquals(new RequestDTO(), "Different type");
    }

    @Test
    void testEquals_newFields() {
        RequestDTO dto1 = new RequestDTO();
        dto1.setRequestLocation("MD");
        dto1.setHelpCategory(new HelpCategoryDto());
        dto1.setIsLeadVolunteer(1);

        assertNotEquals(dto1, new RequestDTO());
    }
}