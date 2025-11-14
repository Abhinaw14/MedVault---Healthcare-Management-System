package com.medvault.demo.controller;

import com.medvault.demo.dto.ChangePasswordDTO;
import com.medvault.demo.dto.DoctorProfileUpdateDTO;
import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.User;
import com.medvault.demo.service.DoctorService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DoctorController(DoctorService doctorService, UserService userService, PasswordEncoder passwordEncoder) {
        this.doctorService = doctorService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get logged-in doctor's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Doctor doctor = doctorService.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", doctor.getId());
            response.put("fullName", doctor.getFullName());
            response.put("specialization", doctor.getSpecialization());
            response.put("contactNumber", doctor.getContactNumber());
            response.put("qualification", doctor.getQualification());
            response.put("yearsOfExperience", doctor.getYearsOfExperience());
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
     * Update logged-in doctor's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody DoctorProfileUpdateDTO updateDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Doctor doctor = doctorService.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

            // Update fields
            doctor.setFullName(updateDTO.getFullName());
            if (updateDTO.getSpecialization() != null) {
                doctor.setSpecialization(updateDTO.getSpecialization());
            }
            doctor.setContactNumber(updateDTO.getContactNumber());
            if (updateDTO.getQualification() != null) {
                doctor.setQualification(updateDTO.getQualification());
            }
            if (updateDTO.getYearsOfExperience() != null) {
                doctor.setYearsOfExperience(updateDTO.getYearsOfExperience());
            }

            Doctor updatedDoctor = doctorService.updateDoctor(doctor);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("doctor", updatedDoctor);

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