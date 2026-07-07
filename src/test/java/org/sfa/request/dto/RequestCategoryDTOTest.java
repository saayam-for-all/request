package org.sfa.request.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {RequestCategoryDTO.class})
@ExtendWith(SpringExtension.class)
class RequestCategoryDTOTest {
    @Autowired
    private RequestCategoryDTO requestCategoryDTO;

    /**
     * Method under test: {@link RequestCategoryDTO#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        // Arrange, Act and Assert
        assertFalse(requestCategoryDTO.canEqual("Other"));
        assertTrue(requestCategoryDTO.canEqual(requestCategoryDTO));
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestCategoryDTO(), null);
        assertNotEquals(new RequestCategoryDTO(), "Different type to RequestCategoryDTO");
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals2() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO(1);

        // Act and Assert
        assertNotEquals(requestCategoryDTO, new RequestCategoryDTO());
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals3() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO();

        // Act and Assert
        assertNotEquals(requestCategoryDTO, new RequestCategoryDTO(1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO();

        // Act and Assert
        assertEquals(requestCategoryDTO, requestCategoryDTO);
        int expectedHashCodeResult = requestCategoryDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestCategoryDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode2() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO();
        RequestCategoryDTO requestCategoryDTO2 = new RequestCategoryDTO();

        // Act and Assert
        assertEquals(requestCategoryDTO, requestCategoryDTO2);
        int expectedHashCodeResult = requestCategoryDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestCategoryDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode3() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO(1);
        RequestCategoryDTO requestCategoryDTO2 = new RequestCategoryDTO(1);

        // Act and Assert
        assertEquals(requestCategoryDTO, requestCategoryDTO2);
        int expectedHashCodeResult = requestCategoryDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestCategoryDTO2.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestCategoryDTO#RequestCategoryDTO()}
     *   <li>{@link RequestCategoryDTO#setRequestCategoryId(Integer)}
     *   <li>{@link RequestCategoryDTO#toString()}
     *   <li>{@link RequestCategoryDTO#getRequestCategoryId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange and Act
        RequestCategoryDTO actualRequestCategoryDTO = new RequestCategoryDTO();
        actualRequestCategoryDTO.setRequestCategoryId(1);
        String actualToStringResult = actualRequestCategoryDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestCategoryDTO(requestCategoryId=1)", actualToStringResult);
        assertEquals(1, actualRequestCategoryDTO.getRequestCategoryId().intValue());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link RequestCategoryDTO#RequestCategoryDTO(Integer)}
     *   <li>{@link RequestCategoryDTO#setRequestCategoryId(Integer)}
     *   <li>{@link RequestCategoryDTO#toString()}
     *   <li>{@link RequestCategoryDTO#getRequestCategoryId()}
     * </ul>
     */
    @Test
    void testGettersAndSetters2() {
        // Arrange and Act
        RequestCategoryDTO actualRequestCategoryDTO = new RequestCategoryDTO(1);
        actualRequestCategoryDTO.setRequestCategoryId(1);
        String actualToStringResult = actualRequestCategoryDTO.toString();

        // Assert that nothing has changed
        assertEquals("RequestCategoryDTO(requestCategoryId=1)", actualToStringResult);
        assertEquals(1, actualRequestCategoryDTO.getRequestCategoryId().intValue());
    }
}
