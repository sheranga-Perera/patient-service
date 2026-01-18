package com.sheranga.patient.service;

import com.sheranga.patient.dto.request.PatientRequest;
import com.sheranga.patient.dto.response.PatientResponse;
import com.sheranga.patient.entity.Patient;
import com.sheranga.patient.exception.ResourceNotFoundException;
import com.sheranga.patient.repository.PatientRepository;
import com.sheranga.patient.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRequest patientRequest;
    private Patient patient;
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

        patient = new Patient();
        patient.setId(patientId);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setAddress("123 Main Street");
        patient.setCity("Colombo");
        patient.setState("Western Province");
        patient.setZipCode("10100");
        patient.setPhoneNumber("0771234567");
        patient.setEmail("john.doe@test.com");

    }

    @Test
    void testCreatePatient_Success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse result = patientService.createPatient(patientRequest);

        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@test.com", result.getEmail());

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testCreatePatient_WithNullFields() {
        PatientRequest requestWithNulls = new PatientRequest(
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null,
                "john.doe@test.com"
        );

        Patient patientWithNulls = new Patient();
        patientWithNulls.setId(patientId);
        patientWithNulls.setFirstName("John");
        patientWithNulls.setLastName("Doe");
        patientWithNulls.setEmail("john.doe@test.com");

        when(patientRepository.save(any(Patient.class))).thenReturn(patientWithNulls);

        PatientResponse result = patientService.createPatient(requestWithNulls);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertNull(result.getAddress());
        assertNull(result.getCity());

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testGetAllPatients_Success() {
        Patient patient2 = new Patient();
        patient2.setId(UUID.randomUUID());
        patient2.setFirstName("Jane");
        patient2.setLastName("Smith");
        patient2.setEmail("jane.smith@test.com");

        List<Patient> patients = Arrays.asList(patient, patient2);
        when(patientRepository.findAll()).thenReturn(patients);

        List<PatientResponse> result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());

        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetAllPatients_EmptyList() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<PatientResponse> result = patientService.getAllPatients();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById_Success() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        PatientResponse result = patientService.getPatientById(patientId);

        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(patientRepository, times(1)).findById(patientId);
    }

    @Test
    void testGetPatientById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatientById(nonExistentId);
        });

        verify(patientRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testUpdatePatient_Success() {
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

        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);
        updatedPatient.setFirstName("John");
        updatedPatient.setLastName("Updated");
        updatedPatient.setAddress("456 New Street");
        updatedPatient.setCity("Kandy");
        updatedPatient.setState("Central Province");
        updatedPatient.setZipCode("20000");
        updatedPatient.setPhoneNumber("0779999999");
        updatedPatient.setEmail("john.updated@test.com");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        PatientResponse result = patientService.updatePatient(patientId, updateRequest);

        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("Updated", result.getLastName());
        assertEquals("john.updated@test.com", result.getEmail());
        assertEquals("Kandy", result.getCity());

        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testUpdatePatient_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(nonExistentId, patientRequest);
        });

        verify(patientRepository, times(1)).findById(nonExistentId);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testDeletePatient_Success() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).delete(patient);

        assertDoesNotThrow(() -> {
            patientService.deletePatient(patientId);
        });

        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, times(1)).delete(patient);
    }

    @Test
    void testDeletePatient_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deletePatient(nonExistentId);
        });

        verify(patientRepository, times(1)).findById(nonExistentId);
        verify(patientRepository, never()).delete(any(Patient.class));
    }
}
