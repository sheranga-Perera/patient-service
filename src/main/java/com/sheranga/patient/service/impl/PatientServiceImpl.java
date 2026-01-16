package com.sheranga.patient.service.impl;


import com.sheranga.patient.dto.request.PatientRequest;
import com.sheranga.patient.dto.response.PatientResponse;
import com.sheranga.patient.entity.Patient;
import com.sheranga.patient.exception.ResourceNotFoundException;
import com.sheranga.patient.repository.PatientRepository;
import com.sheranga.patient.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);
    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
        logger.info("PatientServiceImpl initialized");
    }

    /**
     * This method will create new patient
     * @param request - patients request
     * @return - patient response
     */
    @Override
    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        logger.debug("Creating new patient with email: {}", request.getEmail());
        
        try {
            Patient patient = toEntity(new Patient(), request);
            logger.debug("Patient entity created, saving to database");
            
            Patient savedPatient = patientRepository.save(patient);
            logger.info("Patient saved successfully with ID: {} and email: {}", 
                    savedPatient.getId(), savedPatient.getEmail());
            
            PatientResponse response = toResponse(savedPatient);
            logger.debug("Patient response created for ID: {}", response.getId());
            return response;
        } catch (Exception e) {
            logger.error("Error creating patient with email: {}", request.getEmail(), e);
            throw e;
        }
    }

    /**
     * This method will return all patients
     * @return - list of patients
     */

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        logger.debug("Retrieving all patients from database");
        
        try {
            List<Patient> patients = patientRepository.findAll();
            logger.debug("Found {} patients in database", patients.size());
            
            List<PatientResponse> responses = patients.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            
            logger.info("Successfully retrieved {} patients", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Error retrieving all patients", e);
            throw e;
        }
    }

    /**
     * This method return a patient by id
     * @param id- patient id
     * @return - patient response
     */
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        logger.debug("Retrieving patient with ID: {}", id);
        
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Patient not found with ID: {}", id);
                        return new ResourceNotFoundException("Patient not found with id: " + id);
                    });
            
            logger.info("Patient found with ID: {} - Name: {} {}", 
                    id, patient.getFirstName(), patient.getLastName());
            
            return toResponse(patient);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving patient with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * This method will update the patient under provided id
     * @param id - patient id
     * @param request - patient request
     * @return - updated patient
     */
    @Override
    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        logger.debug("Updating patient with ID: {} - New email: {}", id, request.getEmail());
        
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Patient not found for update with ID: {}", id);
                        return new ResourceNotFoundException("Patient not found with id: " + id);
                    });
            
            logger.debug("Patient found, updating fields. Old email: {}, New email: {}", 
                    patient.getEmail(), request.getEmail());
            
            toEntity(patient, request);
            
            Patient updatedPatient = patientRepository.save(patient);
            logger.info("Patient updated successfully with ID: {} - New email: {}", 
                    id, updatedPatient.getEmail());
            
            return toResponse(updatedPatient);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating patient with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * This method will delete patient by id
     * @param id - patient id
     */
    @Override
    @Transactional
    public void deletePatient(UUID id) {
        logger.debug("Deleting patient with ID: {}", id);
        
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Patient not found for deletion with ID: {}", id);
                        return new ResourceNotFoundException("Patient not found with id: " + id);
                    });
            
            logger.debug("Patient found for deletion - Name: {} {}, Email: {}", 
                    patient.getFirstName(), patient.getLastName(), patient.getEmail());
            
            patientRepository.delete(patient);
            logger.info("Patient deleted successfully with ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting patient with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * This method will map PatientRequest to Patient entity
     * @param request - patient request
     * @return patient
     */
    private Patient toEntity(Patient patient, PatientRequest request) {
        logger.trace("Mapping PatientRequest to Patient entity");
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setEmail(request.getEmail());
        logger.trace("Patient entity mapping completed");
        return patient;
    }

    /**
     *  This method will map Patient Entity to PatientResponse
     * @param patient - patient entity
     * @return response
     */
    private PatientResponse toResponse(Patient patient) {
        logger.trace("Mapping Patient entity to PatientResponse for ID: {}", patient.getId());
        PatientResponse response = new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getAddress(),
                patient.getCity(),
                patient.getState(),
                patient.getZipCode(),
                patient.getPhoneNumber(),
                patient.getEmail()
        );
        logger.trace("PatientResponse mapping completed for ID: {}", response.getId());
        return response;
    }
}
