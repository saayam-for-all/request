package org.sfa.request.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.sfa.request.constant.SaayamStatusCode;

class SaayamResponseDiffblueTest {
    /**
     * Method under test:
     * {@link SaayamResponse#success(SaayamStatusCode, String, Object)}
     */
    @Test
    void testSuccess() {
        // Arrange and Act
        SaayamResponse<Object> actualSuccessResult = SaayamResponse.success(SaayamStatusCode.LAMBDA_TIMEOUT,
                "Not all who wander are lost", "Data");

        // Assert
        assertEquals("Data", actualSuccessResult.getData());
        assertEquals("Not all who wander are lost", actualSuccessResult.getMessage());
        assertEquals("SAAYAM-1000", actualSuccessResult.getSaayamCode());
        assertEquals(200, actualSuccessResult.getStatusCode());
        assertTrue(actualSuccessResult.isSuccess());
    }

    /**
     * Method under test:
     * {@link SaayamResponse#success(SaayamStatusCode, String, Object)}
     */
    @Test
    void testSuccess2() {
        // Arrange and Act
        SaayamResponse<Object> actualSuccessResult = SaayamResponse.success(SaayamStatusCode.REQUEST_CREATED,
                "Not all who wander are lost", "Data");

        // Assert
        assertEquals("Data", actualSuccessResult.getData());
        assertEquals("Not all who wander are lost", actualSuccessResult.getMessage());
        assertEquals("SAAYAM-1401", actualSuccessResult.getSaayamCode());
        assertEquals(201, actualSuccessResult.getStatusCode());
        assertTrue(actualSuccessResult.isSuccess());
    }

    /**
     * Method under test:
     * {@link SaayamResponse#error(int, SaayamStatusCode, String)}
     */
    @Test
    void testError() {
        // Arrange and Act
        SaayamResponse<Object> actualErrorResult = SaayamResponse.error(1, SaayamStatusCode.LAMBDA_TIMEOUT,
                "Not all who wander are lost");

        // Assert
        assertEquals("Not all who wander are lost", actualErrorResult.getMessage());
        assertEquals("SAAYAM-1000", actualErrorResult.getSaayamCode());
        assertNull(actualErrorResult.getData());
        assertEquals(1, actualErrorResult.getStatusCode());
        assertFalse(actualErrorResult.isSuccess());
    }
}
