package com.medvault.demo.controller;

import com.medvault.demo.config.RateLimitService;
import com.medvault.demo.dto.AdminRegisterDTO;
import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.Patient;
import com.medvault.demo.entity.RegistrationRequest;
import com.medvault.demo.entity.RequestStatus;
import com.medvault.demo.service.AdminService;
import com.medvault.demo.service.DoctorService;
import com.medvault.demo.service.PatientService;
import com.medvault.demo.service.RegistrationRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final RegistrationRequestService registrationRequestService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final RateLimitService rateLimitService;

    public AdminController(AdminService adminService, RegistrationRequestService registrationRequestService,
                          DoctorService doctorService, PatientService patientService,
                          RateLimitService rateLimitService) {
        this.adminService = adminService;
        this.registrationRequestService = registrationRequestService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Register Admin (One-time setup - protected with rate limiting)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegisterDTO adminDTO, HttpServletRequest request) {
        try {
            // Rate limiting check
            String clientIp = getClientIpAddress(request);
            if (!rateLimitService.isAllowed("admin_register_" + clientIp)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Too many registration attempts. Please try again later.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

            if (adminService.adminExists()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Admin already exists. Only one admin is allowed.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            adminService.registerAdmin(adminDTO.getFullName(), adminDTO.getContactNumber(), 
                    adminDTO.getUsername(), adminDTO.getPassword());
            rateLimitService.reset("admin_register_" + clientIp); // Reset on success

            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Get all pending registration requests
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests() {
        try {
            List<RegistrationRequest> pendingRequests = registrationRequestService.getAllPendingRequests();
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching pending requests");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all requests by status
     */
    @GetMapping("/requests/status/{status}")
    public ResponseEntity<?> getRequestsByStatus(@PathVariable String status) {
        try {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            List<RegistrationRequest> requests = registrationRequestService.getRequestsByStatus(requestStatus);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid status. Use: PENDING, APPROVED, or REJECTED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching requests");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all registration requests
     */
    @GetMapping("/requests/all")
    public ResponseEntity<?> getAllRequests() {
        try {
            List<RegistrationRequest> allRequests = registrationRequestService.getAllRequests();
            return ResponseEntity.ok(allRequests);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching all requests");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Approve a registration request
     */
    @PostMapping("/requests/approve/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try {
            registrationRequestService.approveRequest(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration request approved successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error approving request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Reject a registration request
     */
    @PostMapping("/requests/reject/{id}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        try {
            registrationRequestService.rejectRequest(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration request rejected successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error rejecting request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all doctors
     */
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching doctors");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all patients
     */
    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching patients");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get doctor by ID
     */
    @GetMapping("/doctors/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            return doctorService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/patients/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            return patientService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching patient");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}