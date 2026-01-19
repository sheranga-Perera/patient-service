package com.sheranga.patient.controller;

import com.sheranga.patient.dto.request.PatientRequest;
import com.sheranga.patient.dto.response.PatientResponse;
import com.sheranga.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * This controller will handle patient related operations
 */
@CrossOrigin(
        origins = "http://localhost:3000",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE }
)
@RestController
@RequestMapping(value="/patient")
@Tag(name = "Patient API", description = "CRUD operations for Patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @Operation(summary = "Create a new patient")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Patient created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PatientResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.createPatient(request));
    }

    @Operation(summary = "Get all patients")
    @ApiResponse(
            responseCode = "200",
            description = "Patients retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PatientResponse.class))
            )
    )
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @Operation(summary = "Get patient by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Patient found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PatientResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @Operation(summary = "Update patient by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Patient updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PatientResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Patient not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable UUID id, @Valid @RequestBody PatientRequest patientRequest){
        return ResponseEntity.ok(patientService.updatePatient(id, patientRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}