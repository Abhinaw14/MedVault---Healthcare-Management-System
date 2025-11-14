package com.medvault.demo.controller;

import com.medvault.demo.dto.ChangePasswordDTO;
import com.medvault.demo.dto.PatientProfileUpdateDTO;
import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.Patient;
import com.medvault.demo.entity.User;
import com.medvault.demo.service.DoctorService;
import com.medvault.demo.service.PatientService;
import com.medvault.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;
    private final UserService userService;
    private final DoctorService doctorService;
    private final PasswordEncoder passwordEncoder;

    public PatientController(PatientService patientService, UserService userService,
                            DoctorService doctorService, PasswordEncoder passwordEncoder) {
        this.patientService = patientService;
        this.userService = userService;
        this.doctorService = doctorService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get logged-in patient's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Patient patient = patientService.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", patient.getId());
            response.put("fullName", patient.getFullName());
            response.put("age", patient.getAge());
            response.put("gender", patient.getGender());
            response.put("contactNumber", patient.getContactNumber());
            response.put("address", patient.getAddress());
            response.put("email", user.getUsername());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update logged-in patient's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody PatientProfileUpdateDTO updateDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Patient patient = patientService.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));

            // Update fields
            patient.setFullName(updateDTO.getFullName());
            Integer age = updateDTO.getAge();
            if (age != null) {
                patient.setAge(age);
            }
            if (updateDTO.getGender() != null) {
                patient.setGender(updateDTO.getGender());
            }
            patient.setContactNumber(updateDTO.getContactNumber());
            if (updateDTO.getAddress() != null) {
                patient.setAddress(updateDTO.getAddress());
            }

            Patient updatedPatient = patientService.updatePatient(patient);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("patient", updatedPatient);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get list of all available doctors (for reference/browsing)
     */
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();

            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctors);
            response.put("count", doctors.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching doctors");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a specific doctor's details by ID
     */
    @GetMapping("/doctors/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            return doctorService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error fetching doctor details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO passwordDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify current password
            if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Update password
            userService.updatePassword(user, passwordDTO.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error changing password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}