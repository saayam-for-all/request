package org.sfa.request.exception.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class BaseExceptionTest {
    /**
     * Method under test:
     * {@link BaseException#BaseException(String, Throwable, Object[])}
     */
    @Test
    void testNewBaseException() {
        // Arrange
        Throwable cause = new Throwable();
        Object[] params = new Object[]{"Params"};

        // Act
        BaseException actualBaseException = new BaseException("An error occurred", cause, params);

        // Assert
        assertEquals("An error occurred", actualBaseException.getLocalizedMessage());
        assertEquals("An error occurred", actualBaseException.getMessage());
        Object[] params2 = actualBaseException.getParams();
        assertEquals("Params", params2[0]);
        Throwable cause2 = actualBaseException.getCause();
        assertNull(cause2.getLocalizedMessage());
        assertNull(cause2.getMessage());
        assertNull(cause2.getCause());
        Throwable[] suppressed = actualBaseException.getSuppressed();
        assertEquals(0, suppressed.length);
        assertEquals(1, params2.length);
        assertSame(cause, cause2);
        assertSame(suppressed, cause2.getSuppressed());
        assertSame(params, params2);
    }

    /**
     * Method under test: {@link BaseException#BaseException(String, Object[])}
     */
    @Test
    void testNewBaseException2() {
        // Arrange
        Object[] params = new Object[]{"Params"};

        // Act
        BaseException actualBaseException = new BaseException("An error occurred", params);

        // Assert
        assertEquals("An error occurred", actualBaseException.getLocalizedMessage());
        assertEquals("An error occurred", actualBaseException.getMessage());
        Object[] params2 = actualBaseException.getParams();
        assertEquals("Params", params2[0]);
        assertNull(actualBaseException.getCause());
        assertEquals(0, actualBaseException.getSuppressed().length);
        assertEquals(1, params2.length);
        assertSame(params, params2);
    }
}
