package com.medvault.demo.controller;

import com.medvault.demo.dto.RegistrationRequestDTO;
import com.medvault.demo.entity.RegistrationRequest;
import com.medvault.demo.service.RegistrationRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "*")
public class RegistrationRequestController {

    private final RegistrationRequestService registrationRequestService;

    public RegistrationRequestController(RegistrationRequestService registrationRequestService) {
        this.registrationRequestService = registrationRequestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitRegistrationRequest(@Valid @RequestBody RegistrationRequestDTO requestDTO) {
        try {
            RegistrationRequest savedRequest = registrationRequestService.createRegistrationRequest(requestDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration request submitted successfully");
            response.put("email", savedRequest.getEmail());
            response.put("status", savedRequest.getStatus());
            response.put("id", savedRequest.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/status/{email}")
    public ResponseEntity<?> checkRegistrationStatus(@PathVariable String email) {
        try {
            Optional<RegistrationRequest> request = registrationRequestService.findByEmail(email);

            if (request.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No registration request found for this email");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("email", request.get().getEmail());
            response.put("fullName", request.get().getFullName());
            response.put("status", request.get().getStatus());
            response.put("requestedRole", request.get().getRequestedRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error checking registration status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}