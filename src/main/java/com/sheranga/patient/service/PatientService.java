package com.sheranga.patient.service;

import com.sheranga.patient.dto.request.PatientRequest;
import com.sheranga.patient.dto.response.PatientResponse;

import java.util.List;
import java.util.UUID;

/**
 * This service handles patient related operations
 */
public interface PatientService {

    PatientResponse createPatient(PatientRequest request);
    List<PatientResponse> getAllPatients();
    PatientResponse getPatientById(UUID id);
    PatientResponse updatePatient(UUID id, PatientRequest request);

    void deletePatient(UUID id);
}