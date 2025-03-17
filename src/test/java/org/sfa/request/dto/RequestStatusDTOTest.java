package org.sfa.request.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {RequestStatusDTO.class})
@ExtendWith(SpringExtension.class)
class RequestStatusDTOTest {
    @Autowired
    private RequestStatusDTO requestStatusDTO;

    /**
     * Method under test: {@link RequestStatusDTO#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        // Arrange, Act and Assert
        assertFalse(requestStatusDTO.canEqual("Other"));
        assertTrue(requestStatusDTO.canEqual(requestStatusDTO));
    }

    /**
     * Method under test: {@link RequestStatusDTO#equals(Object)}
     */
    @Test
    void testEquals() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestStatusDTO(), null);
        assertNotEquals(new RequestStatusDTO(), "Different type to RequestStatusDTO");
    }

    /**
     * Method under test: {@link RequestStatusDTO#equals(Object)}
     */
    @Test
    void testEquals2() {
        // Arrange
        RequestStatusDTO requestStatusDTO = new RequestStatusDTO(1);

        // Act and Assert
        assertNotEquals(requestStatusDTO, new RequestStatusDTO());
    }

    /**
     * Method under test: {@link RequestStatusDTO#equals(Object)}
     */
    @Test
    void testEquals3() {
        // Arrange
        RequestStatusDTO requestStatusDTO = new RequestStatusDTO();

        // Act and Assert
        assertNotEquals(requestStatusDTO, new RequestStatusDTO(1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestStatusDTO#equals(Object)}
     *   <li>{@link RequestStatusDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        RequestStatusDTO requestStatusDTO = new RequestStatusDTO();

        // Act and Assert
        assertEquals(requestStatusDTO, requestStatusDTO);
        int expectedHashCodeResult = requestStatusDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestStatusDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestStatusDTO#equals(Object)}
     *   <li>{@link RequestStatusDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode2() {
        // Arrange
        RequestStatusDTO requestStatusDTO = new RequestStatusDTO();
        RequestStatusDTO requestStatusDTO2 = new RequestStatusDTO();

        // Act and Assert
        assertEquals(requestStatusDTO, requestStatusDTO2);
        int expectedHashCodeResult = requestStatusDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestStatusDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestStatusDTO#equals(Object)}
     *   <li>{@link RequestStatusDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode3() {
        // Arrange
        RequestStatusDTO requestStatusDTO = new RequestStatusDTO(1);
        RequestStatusDTO requestStatusDTO2 = new RequestStatusDTO(1);

        // Act and Assert
        assertEquals(requestStatusDTO, requestStatusDTO2);
        int expectedHashCodeResult = requestStatusDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestStatusDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestStatusDTO#RequestStatusDTO()}
     *   <li>{@link RequestStatusDTO#setRequestStatusId(Integer)}
     *   <li>{@link RequestStatusDTO#toString()}
     *   <li>{@link RequestStatusDTO#getRequestStatusId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        RequestStatusDTO actualRequestStatusDTO = new RequestStatusDTO();
        actualRequestStatusDTO.setRequestStatusId(1);
        String actualToStringResult = actualRequestStatusDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestStatusDTO(requestStatusId=1)", actualToStringResult);
        assertEquals(1, actualRequestStatusDTO.getRequestStatusId().intValue());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestStatusDTO#RequestStatusDTO(Integer)}
     *   <li>{@link RequestStatusDTO#setRequestStatusId(Integer)}
     *   <li>{@link RequestStatusDTO#toString()}
     *   <li>{@link RequestStatusDTO#getRequestStatusId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters2() {
        // Arrange and Act
        RequestStatusDTO actualRequestStatusDTO = new RequestStatusDTO(1);
        actualRequestStatusDTO.setRequestStatusId(1);
        String actualToStringResult = actualRequestStatusDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestStatusDTO(requestStatusId=1)", actualToStringResult);
        assertEquals(1, actualRequestStatusDTO.getRequestStatusId().intValue());
    }
}
