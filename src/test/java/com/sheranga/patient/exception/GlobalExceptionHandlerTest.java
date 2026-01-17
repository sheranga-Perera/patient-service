package com.sheranga.patient.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/patient");
    }

    @Test
    void testHandleNotFound_Success() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient not found with id: 123");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Patient not found with id: 123", response.getBody().getMessage());
        assertEquals("/patient", response.getBody().getPath());
    }

    @Test
    void testHandleValidation_Success() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("patientRequest", "firstName", "First name is required");
        FieldError fieldError2 = new FieldError("patientRequest", "email", "Email is invalid");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("Invalid request payload", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertTrue(response.getBody().getValidationErrors().contains("firstName: First name is required"));
        assertTrue(response.getBody().getValidationErrors().contains("email: Email is invalid"));
    }

    @Test
    void testHandleConflict_Success() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("duplicate key");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals(
                "Request violates data integrity constraints",
                response.getBody().getMessage()
        );
        assertEquals("/patient", response.getBody().getPath());
    }

    @Test
    void testHandleGeneric_Success() {
        RuntimeException ex = new RuntimeException("Anything");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Unexpected server error", response.getBody().getMessage());
        assertEquals("/patient", response.getBody().getPath());
    }

    @Test
    void testHandleGeneric_NullRequestURI() {
        when(request.getRequestURI()).thenReturn(null);
        RuntimeException ex = new RuntimeException("Test exception");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
