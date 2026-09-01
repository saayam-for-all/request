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

@ContextConfiguration(classes = {RequestPriorityDTO.class})
@ExtendWith(SpringExtension.class)
class RequestPriorityDTOTest {
    @Autowired
    private RequestPriorityDTO requestPriorityDTO;

    /**
     * Method under test: {@link RequestPriorityDTO#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        // Arrange, Act and Assert
        assertFalse(requestPriorityDTO.canEqual("Other"));
        assertTrue(requestPriorityDTO.canEqual(requestPriorityDTO));
    }

    /**
     * Method under test: {@link RequestPriorityDTO#equals(Object)}
     */
    @Test
    void testEquals() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestPriorityDTO(), null);
        assertNotEquals(new RequestPriorityDTO(), "Different type to RequestPriorityDTO");
    }

    /**
     * Method under test: {@link RequestPriorityDTO#equals(Object)}
     */
    @Test
    void testEquals2() {
        // Arrange
        RequestPriorityDTO requestPriorityDTO = new RequestPriorityDTO(1);

        // Act and Assert
        assertNotEquals(requestPriorityDTO, new RequestPriorityDTO());
    }

    /**
     * Method under test: {@link RequestPriorityDTO#equals(Object)}
     */
    @Test
    void testEquals3() {
        // Arrange
        RequestPriorityDTO requestPriorityDTO = new RequestPriorityDTO();

        // Act and Assert
        assertNotEquals(requestPriorityDTO, new RequestPriorityDTO(1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestPriorityDTO#equals(Object)}
     *   <li>{@link RequestPriorityDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        RequestPriorityDTO requestPriorityDTO = new RequestPriorityDTO();

        // Act and Assert
        assertEquals(requestPriorityDTO, requestPriorityDTO);
        int expectedHashCodeResult = requestPriorityDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestPriorityDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestPriorityDTO#equals(Object)}
     *   <li>{@link RequestPriorityDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode2() {
        // Arrange
        RequestPriorityDTO requestPriorityDTO = new RequestPriorityDTO();
        RequestPriorityDTO requestPriorityDTO2 = new RequestPriorityDTO();

        // Act and Assert
        assertEquals(requestPriorityDTO, requestPriorityDTO2);
        int expectedHashCodeResult = requestPriorityDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestPriorityDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestPriorityDTO#equals(Object)}
     *   <li>{@link RequestPriorityDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode3() {
        // Arrange
        RequestPriorityDTO requestPriorityDTO = new RequestPriorityDTO(1);
        RequestPriorityDTO requestPriorityDTO2 = new RequestPriorityDTO(1);

        // Act and Assert
        assertEquals(requestPriorityDTO, requestPriorityDTO2);
        int expectedHashCodeResult = requestPriorityDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestPriorityDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestPriorityDTO#RequestPriorityDTO()}
     *   <li>{@link RequestPriorityDTO#setRequestPriorityId(Integer)}
     *   <li>{@link RequestPriorityDTO#toString()}
     *   <li>{@link RequestPriorityDTO#getRequestPriorityId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        RequestPriorityDTO actualRequestPriorityDTO = new RequestPriorityDTO();
        actualRequestPriorityDTO.setRequestPriorityId(1);
        String actualToStringResult = actualRequestPriorityDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestPriorityDTO(requestPriorityId=1)", actualToStringResult);
        assertEquals(1, actualRequestPriorityDTO.getRequestPriorityId().intValue());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestPriorityDTO#RequestPriorityDTO(Integer)}
     *   <li>{@link RequestPriorityDTO#setRequestPriorityId(Integer)}
     *   <li>{@link RequestPriorityDTO#toString()}
     *   <li>{@link RequestPriorityDTO#getRequestPriorityId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters2() {
        // Arrange and Act
        RequestPriorityDTO actualRequestPriorityDTO = new RequestPriorityDTO(1);
        actualRequestPriorityDTO.setRequestPriorityId(1);
        String actualToStringResult = actualRequestPriorityDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestPriorityDTO(requestPriorityId=1)", actualToStringResult);
        assertEquals(1, actualRequestPriorityDTO.getRequestPriorityId().intValue());
    }
}
