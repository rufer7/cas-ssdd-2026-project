package ch.ssdd.eventhub.adapters.inbound.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CustomErrorControllerTest {

    private CustomErrorController errorController;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        errorController = new CustomErrorController();
        request = mock(HttpServletRequest.class);
    }

    @Test
    void shouldReturnBadRequestDetails_WhenStatusCodeIs400() {
        // given
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(400);

        // when
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Invalid character encoding in URL query parameter.", body.get("message"));
    }

    @Test
    void shouldReturnGenericInternalErrorDetails_WhenStatusCodeIs500() {
        // given
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(500);

        // when
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred processing the request.", body.get("message"));
    }

    @Test
    void shouldFallbackTo500InternalError_WhenStatusCodeAttributeIsNull() {
        // given
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);

        // when
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred processing the request.", body.get("message"));
    }

    @Test
    void shouldReturnCorrectStatusAndGenericDetails_WhenStatusCodeIsCustomOrOtherThan400() {
        // given
        int notFoundStatus = 404;
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(notFoundStatus);

        // when
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred processing the request.", body.get("message"));
    }
}
