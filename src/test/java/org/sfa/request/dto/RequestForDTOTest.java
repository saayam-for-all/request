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

@ContextConfiguration(classes = {RequestForDTO.class})
@ExtendWith(SpringExtension.class)
class RequestForDTOTest {
    @Autowired
    private RequestForDTO requestForDTO;

    /**
     * Method under test: {@link RequestForDTO#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        // Arrange, Act and Assert
        assertFalse(requestForDTO.canEqual("Other"));
        assertTrue(requestForDTO.canEqual(requestForDTO));
    }

    /**
     * Method under test: {@link RequestForDTO#equals(Object)}
     */
    @Test
    void testEquals() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestForDTO(), null);
        assertNotEquals(new RequestForDTO(), "Different type to RequestForDTO");
    }

    /**
     * Method under test: {@link RequestForDTO#equals(Object)}
     */
    @Test
    void testEquals2() {
        // Arrange
        RequestForDTO requestForDTO = new RequestForDTO(1);

        // Act and Assert
        assertNotEquals(requestForDTO, new RequestForDTO());
    }

    /**
     * Method under test: {@link RequestForDTO#equals(Object)}
     */
    @Test
    void testEquals3() {
        // Arrange
        RequestForDTO requestForDTO = new RequestForDTO();

        // Act and Assert
        assertNotEquals(requestForDTO, new RequestForDTO(1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestForDTO#equals(Object)}
     *   <li>{@link RequestForDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        RequestForDTO requestForDTO = new RequestForDTO();

        // Act and Assert
        assertEquals(requestForDTO, requestForDTO);
        int expectedHashCodeResult = requestForDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestForDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestForDTO#equals(Object)}
     *   <li>{@link RequestForDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode2() {
        // Arrange
        RequestForDTO requestForDTO = new RequestForDTO();
        RequestForDTO requestForDTO2 = new RequestForDTO();

        // Act and Assert
        assertEquals(requestForDTO, requestForDTO2);
        int expectedHashCodeResult = requestForDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestForDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestForDTO#equals(Object)}
     *   <li>{@link RequestForDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode3() {
        // Arrange
        RequestForDTO requestForDTO = new RequestForDTO(1);
        RequestForDTO requestForDTO2 = new RequestForDTO(1);

        // Act and Assert
        assertEquals(requestForDTO, requestForDTO2);
        int expectedHashCodeResult = requestForDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestForDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestForDTO#RequestForDTO()}
     *   <li>{@link RequestForDTO#setRequestForId(Integer)}
     *   <li>{@link RequestForDTO#toString()}
     *   <li>{@link RequestForDTO#getRequestForId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        RequestForDTO actualRequestForDTO = new RequestForDTO();
        actualRequestForDTO.setRequestForId(1);
        String actualToStringResult = actualRequestForDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestForDTO(requestForId=1)", actualToStringResult);
        assertEquals(1, actualRequestForDTO.getRequestForId().intValue());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestForDTO#RequestForDTO(Integer)}
     *   <li>{@link RequestForDTO#setRequestForId(Integer)}
     *   <li>{@link RequestForDTO#toString()}
     *   <li>{@link RequestForDTO#getRequestForId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters2() {
        // Arrange and Act
        RequestForDTO actualRequestForDTO = new RequestForDTO(1);
        actualRequestForDTO.setRequestForId(1);
        String actualToStringResult = actualRequestForDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestForDTO(requestForId=1)", actualToStringResult);
        assertEquals(1, actualRequestForDTO.getRequestForId().intValue());
    }
}
