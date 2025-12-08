package org.sfa.request.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfa.request.exception.types.ConflictException;
import org.sfa.request.exception.types.EnumUnspecifiedException;
import org.sfa.request.exception.types.ForbiddenException;
import org.sfa.request.exception.types.InvalidRequestException;
import org.sfa.request.exception.types.NotFoundException;
import org.sfa.request.exception.types.UnauthorizedException;
import org.sfa.request.response.SaayamResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {GlobalExceptionHandler.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class GlobalExceptionHandlerTest {
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @MockBean
    private MessageSource messageSource;

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleInvalidRequestException(InvalidRequestException, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleInvalidRequestException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   org.springframework.context.NoSuchMessageException: No message found under code 'error.invalidRequest' for locale 'en_US'.
        //       at org.sfa.request.exception.handler.GlobalExceptionHandler.handleInvalidRequestException(GlobalExceptionHandler.java:34)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        InvalidRequestException ex = new InvalidRequestException("An error occurred");

        // Act
        globalExceptionHandler.handleInvalidRequestException(ex, Locale.getDefault());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleNotFoundException(NotFoundException, Locale)}
     */
    @Test
    void testHandleNotFoundException() {
        // Arrange
        NotFoundException ex = new NotFoundException("An error occurred");

        // Act
        ResponseEntity<SaayamResponse<Void>> actualHandleNotFoundExceptionResult = globalExceptionHandler
                .handleNotFoundException(ex, Locale.getDefault());

        // Assert
        SaayamResponse<Void> body = actualHandleNotFoundExceptionResult.getBody();
        assertEquals("An error occurred", body.getMessage());
        assertEquals("SAAYAM-1502", body.getSaayamCode());
        assertNull(body.getData());
        assertEquals(404, body.getStatusCode());
        assertEquals(404, actualHandleNotFoundExceptionResult.getStatusCodeValue());
        assertFalse(body.isSuccess());
        assertTrue(actualHandleNotFoundExceptionResult.hasBody());
        assertTrue(actualHandleNotFoundExceptionResult.getHeaders().isEmpty());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleConflictException(ConflictException, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleConflictException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   org.springframework.context.NoSuchMessageException: No message found under code 'error.conflict' for locale 'en_US'.
        //       at org.sfa.request.exception.handler.GlobalExceptionHandler.handleConflictException(GlobalExceptionHandler.java:60)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        ConflictException ex = new ConflictException("An error occurred");

        // Act
        globalExceptionHandler.handleConflictException(ex, Locale.getDefault());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleForbiddenException(ForbiddenException, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleForbiddenException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   org.springframework.context.NoSuchMessageException: No message found under code 'error.forbidden' for locale 'en_US'.
        //       at org.sfa.request.exception.handler.GlobalExceptionHandler.handleForbiddenException(GlobalExceptionHandler.java:73)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        ForbiddenException ex = new ForbiddenException("An error occurred");

        // Act
        globalExceptionHandler.handleForbiddenException(ex, Locale.getDefault());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleUnauthorizedException(UnauthorizedException, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleUnauthorizedException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   org.springframework.context.NoSuchMessageException: No message found under code 'error.unauthorized' for locale 'en_US'.
        //       at org.sfa.request.exception.handler.GlobalExceptionHandler.handleUnauthorizedException(GlobalExceptionHandler.java:86)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        UnauthorizedException ex = new UnauthorizedException("An error occurred");

        // Act
        globalExceptionHandler.handleUnauthorizedException(ex, Locale.getDefault());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleEnumUnspecifiedException(EnumUnspecifiedException, Locale)}
     */
    @Test
    void testHandleEnumUnspecifiedException() {
        // Arrange
        EnumUnspecifiedException ex = new EnumUnspecifiedException("An error occurred");

        // Act
        ResponseEntity<SaayamResponse<Void>> actualHandleEnumUnspecifiedExceptionResult = globalExceptionHandler
                .handleEnumUnspecifiedException(ex, Locale.getDefault());

        // Assert
        SaayamResponse<Void> body = actualHandleEnumUnspecifiedExceptionResult.getBody();
        assertEquals("An error occurred", body.getMessage());
        assertEquals("SAAYAM-1513", body.getSaayamCode());
        assertNull(body.getData());
        assertEquals(400, body.getStatusCode());
        assertEquals(400, actualHandleEnumUnspecifiedExceptionResult.getStatusCodeValue());
        assertFalse(body.isSuccess());
        assertTrue(actualHandleEnumUnspecifiedExceptionResult.hasBody());
        assertTrue(actualHandleEnumUnspecifiedExceptionResult.getHeaders().isEmpty());
    }

    /**
     * Method under test:
     * {@link GlobalExceptionHandler#handleGeneralException(Exception, Locale)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testHandleGeneralException() {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Reason: No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   org.springframework.context.NoSuchMessageException: No message found under code 'error.general' for locale 'en_US'.
        //       at org.sfa.request.exception.handler.GlobalExceptionHandler.handleGeneralException(GlobalExceptionHandler.java:113)
        //   See https://diff.blue/R013 to resolve this issue.

        // Arrange
        Exception ex = new Exception("foo");

        // Act
        globalExceptionHandler.handleGeneralException(ex, Locale.getDefault());
    }
}
