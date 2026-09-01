package org.sfa.request.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RequestCategoryDTODiffblueTest {
    /**
     * Methods under test:
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode_whenOtherIsEqual_thenReturnEqual() {
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
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode_whenOtherIsEqual_thenReturnEqual2() {
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
     * <ul>
     *   <li>{@link RequestCategoryDTO#equals(Object)}
     *   <li>{@link RequestCategoryDTO#hashCode()}
     * </ul>
     */
    @Test
    void testEqualsAndHashCode_whenOtherIsSame_thenReturnEqual() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO();

        // Act and Assert
        assertEquals(requestCategoryDTO, requestCategoryDTO);
        int expectedHashCodeResult = requestCategoryDTO.hashCode();
        assertEquals(expectedHashCodeResult, requestCategoryDTO.hashCode());
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals_whenOtherIsDifferent_thenReturnNotEqual() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO(1);

        // Act and Assert
        assertNotEquals(requestCategoryDTO, new RequestCategoryDTO());
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals_whenOtherIsDifferent_thenReturnNotEqual2() {
        // Arrange
        RequestCategoryDTO requestCategoryDTO = new RequestCategoryDTO();

        // Act and Assert
        assertNotEquals(requestCategoryDTO, new RequestCategoryDTO(1));
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals_whenOtherIsNull_thenReturnNotEqual() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestCategoryDTO(), null);
    }

    /**
     * Method under test: {@link RequestCategoryDTO#equals(Object)}
     */
    @Test
    void testEquals_whenOtherIsWrongType_thenReturnNotEqual() {
        // Arrange, Act and Assert
        assertNotEquals(new RequestCategoryDTO(), "Different type to RequestCategoryDTO");
    }

    /**
     * Methods under test:
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
