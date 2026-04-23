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

@ContextConfiguration(classes = {RequestTypeDTO.class})
@ExtendWith(SpringExtension.class)
class RequestTypeDTOTest {
    @Autowired
    private RequestTypeDTO requestTypeDTO;

    /**
     * Method under test: {@link RequestTypeDTO#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        // Arrange, Act and Assert
        assertFalse(requestTypeDTO.canEqual("Other"));
        assertTrue(requestTypeDTO.canEqual(requestTypeDTO));
    }

    /**
     * Method under test: {@link RequestTypeDTO#equals(Object)}
     */
    @Test
    void testEquals() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestTypeDTO(), null);
        assertNotEquals(new RequestTypeDTO(), "Different type to RequestTypeDTO");
    }

    /**
     * Method under test: {@link RequestTypeDTO#equals(Object)}
     */
    @Test
    void testEquals2() {
        // Arrange
        RequestTypeDTO requestTypeDTO = new RequestTypeDTO(1);

        // Act and Assert
        assertNotEquals(requestTypeDTO, new RequestTypeDTO());
    }

    /**
     * Method under test: {@link RequestTypeDTO#equals(Object)}
     */
    @Test
    void testEquals3() {
        // Arrange
        RequestTypeDTO requestTypeDTO = new RequestTypeDTO();

        // Act and Assert
        assertNotEquals(requestTypeDTO, new RequestTypeDTO(1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestTypeDTO#equals(Object)}
     *   <li>{@link RequestTypeDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        RequestTypeDTO requestTypeDTO = new RequestTypeDTO();

        // Act and Assert
        assertEquals(requestTypeDTO, requestTypeDTO);
        int expectedHashCodeResult = requestTypeDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestTypeDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestTypeDTO#equals(Object)}
     *   <li>{@link RequestTypeDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode2() {
        // Arrange
        RequestTypeDTO requestTypeDTO = new RequestTypeDTO();
        RequestTypeDTO requestTypeDTO2 = new RequestTypeDTO();

        // Act and Assert
        assertEquals(requestTypeDTO, requestTypeDTO2);
        int expectedHashCodeResult = requestTypeDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestTypeDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestTypeDTO#equals(Object)}
     *   <li>{@link RequestTypeDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode3() {
        // Arrange
        RequestTypeDTO requestTypeDTO = new RequestTypeDTO(1);
        RequestTypeDTO requestTypeDTO2 = new RequestTypeDTO(1);

        // Act and Assert
        assertEquals(requestTypeDTO, requestTypeDTO2);
        int expectedHashCodeResult = requestTypeDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestTypeDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestTypeDTO#RequestTypeDTO()}
     *   <li>{@link RequestTypeDTO#setRequestTypeId(Integer)}
     *   <li>{@link RequestTypeDTO#toString()}
     *   <li>{@link RequestTypeDTO#getRequestTypeId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        RequestTypeDTO actualRequestTypeDTO = new RequestTypeDTO();
        actualRequestTypeDTO.setRequestTypeId(1);
        String actualToStringResult = actualRequestTypeDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestTypeDTO(requestTypeId=1)", actualToStringResult);
        assertEquals(1, actualRequestTypeDTO.getRequestTypeId().intValue());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestTypeDTO#RequestTypeDTO(Integer)}
     *   <li>{@link RequestTypeDTO#setRequestTypeId(Integer)}
     *   <li>{@link RequestTypeDTO#toString()}
     *   <li>{@link RequestTypeDTO#getRequestTypeId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters2() {
        // Arrange and Act
        RequestTypeDTO actualRequestTypeDTO = new RequestTypeDTO(1);
        actualRequestTypeDTO.setRequestTypeId(1);
        String actualToStringResult = actualRequestTypeDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestTypeDTO(requestTypeId=1)", actualToStringResult);
        assertEquals(1, actualRequestTypeDTO.getRequestTypeId().intValue());
    }
}
