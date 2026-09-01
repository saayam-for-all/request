package org.sfa.request.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SaayamStatusCodeDiffblueTest {
    /**
     * Methods under test:
     * <ul>
     *   <li>{@link SaayamStatusCode#getCode()}
     *   <li>{@link SaayamStatusCode#getDefaultMessage()}
     * </ul>
     */
    @Test
    void testGettersAndSetters() {
        // Arrange
        SaayamStatusCode valueOfResult = SaayamStatusCode.valueOf("LAMBDA_TIMEOUT");

        // Act
        String actualCode = valueOfResult.getCode();

        // Assert
        assertEquals("Lambda function timed out", valueOfResult.getDefaultMessage());
        assertEquals("SAAYAM-1000", actualCode);
    }
}
