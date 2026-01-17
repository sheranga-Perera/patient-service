package com.sheranga.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheranga.patient.dto.request.PatientRequest;
import com.sheranga.patient.dto.response.PatientResponse;
import com.sheranga.patient.exception.GlobalExceptionHandler;
import com.sheranga.patient.exception.ResourceNotFoundException;
import com.sheranga.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientRequest patientRequest;
    private PatientResponse patientResponse;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        
        patientRequest = new PatientRequest(
                "John",
                "Doe",
                "123 Main Street",
                "Colombo",
                "Western Province",
                "10100",
                "0771234567",
                "john.doe@test.com"
        );

        patientResponse = new PatientResponse(
                patientId,
                "John",
                "Doe",
                "123 Main Street",
                "Colombo",
                "Western Province",
                "10100",
                "0771234567",
                "john.doe@test.com"
        );
    }

    @Test
    void testCreatePatient_Success() throws Exception {
        when(patientService.createPatient(any(PatientRequest.class))).thenReturn(patientResponse);

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));

        verify(patientService, times(1)).createPatient(any(PatientRequest.class));
    }

    @Test
    void testCreatePatient_ValidationError() throws Exception {
        PatientRequest invalidRequest = new PatientRequest(
                "", // Empty first name
                "Doe",
                "123 Main Street",
                "Colombo",
                "Western Province",
                "10100",
                "0771234567",
                "invalid-email" // Invalid email
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.validationErrors").isArray());

        verify(patientService, never()).createPatient(any(PatientRequest.class));
    }

    @Test
    void testGetAllPatients_Success() throws Exception {
        PatientResponse patient2 = new PatientResponse(
                UUID.randomUUID(),
                "Jane",
                "Smith",
                "456 Oak Avenue",
                "Kandy",
                "Central Province",
                "20000",
                "0779876543",
                "jane.smith@test.com"
        );

        List<PatientResponse> patients = Arrays.asList(patientResponse, patient2);
        when(patientService.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    void testGetAllPatients_EmptyList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    void testGetPatientById_Success() throws Exception {
        when(patientService.getPatientById(patientId)).thenReturn(patientResponse);

        mockMvc.perform(get("/patient/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));

        verify(patientService, times(1)).getPatientById(patientId);
    }

    @Test
    void testGetPatientById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(patientService.getPatientById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: " + nonExistentId));

        mockMvc.perform(get("/patient/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).getPatientById(nonExistentId);
    }

    @Test
    void testUpdatePatient_Success() throws Exception {
        PatientRequest updateRequest = new PatientRequest(
                "John",
                "Updated",
                "456 New Street",
                "Kandy",
                "Central Province",
                "20000",
                "0779999999",
                "john.updated@test.com"
        );

        PatientResponse updatedResponse = new PatientResponse(
                patientId,
                "John",
                "Updated",
                "456 New Street",
                "Kandy",
                "Central Province",
                "20000",
                "0779999999",
                "john.updated@test.com"
        );

        when(patientService.updatePatient(eq(patientId), any(PatientRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/patient/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@test.com"));

        verify(patientService, times(1)).updatePatient(eq(patientId), any(PatientRequest.class));
    }

    @Test
    void testUpdatePatient_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(patientService.updatePatient(eq(nonExistentId), any(PatientRequest.class)))
                .thenThrow(new ResourceNotFoundException("Patient not found with id: " + nonExistentId));

        mockMvc.perform(put("/patient/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).updatePatient(eq(nonExistentId), any(PatientRequest.class));
    }

    @Test
    void testUpdatePatient_ValidationError() throws Exception {
        PatientRequest invalidRequest = new PatientRequest(
                "", // Empty first name
                "Doe",
                null,
                null,
                null,
                null,
                null,
                "invalid-email"
        );

        mockMvc.perform(put("/patient/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).updatePatient(any(UUID.class), any(PatientRequest.class));
    }

    @Test
    void testDeletePatient_Success() throws Exception {
        doNothing().when(patientService).deletePatient(patientId);

        mockMvc.perform(delete("/patient/{id}", patientId))
                .andExpect(status().isNoContent());

        verify(patientService, times(1)).deletePatient(patientId);
    }

    @Test
    void testDeletePatient_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Patient not found with id: " + nonExistentId))
                .when(patientService).deletePatient(nonExistentId);

        mockMvc.perform(delete("/patient/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(patientService, times(1)).deletePatient(nonExistentId);
    }
}
