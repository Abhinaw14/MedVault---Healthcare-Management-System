package com.medvault.demo.service;

import com.medvault.demo.entity.*;
import com.medvault.demo.repository.RegistrationRequestRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserService userService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationRequestService(RegistrationRequestRepository registrationRequestRepository,
                                     UserService userService, DoctorService doctorService,
                                     PatientService patientService, EmailService emailService,
                                     PasswordEncoder passwordEncoder) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.userService = userService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    public RegistrationRequest createRegistrationRequest(RegistrationRequest request) {
        // Check if email already exists in requests or users
        if (registrationRequestRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Registration request with this email already exists.");
        }
        if (userService.existsByUsername(request.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        request.setStatus(RequestStatus.PENDING);
        RegistrationRequest saved = registrationRequestRepository.save(request);

        // Send confirmation email
        emailService.sendRegistrationConfirmation(request.getEmail(), request.getFullName());

        return saved;
    }

    public RegistrationRequest createRegistrationRequest(com.medvault.demo.dto.RegistrationRequestDTO requestDTO) {
        // Check if email already exists in requests or users
        if (registrationRequestRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("Registration request with this email already exists.");
        }
        if (userService.existsByUsername(requestDTO.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        RegistrationRequest request = new RegistrationRequest(
                requestDTO.getFullName(),
                requestDTO.getEmail(),
                requestDTO.getMobileNumber(),
                requestDTO.getRequestedRole()
        );
        request.setStatus(RequestStatus.PENDING);
        RegistrationRequest saved = registrationRequestRepository.save(request);

        // Send confirmation email
        emailService.sendRegistrationConfirmation(request.getEmail(), request.getFullName());

        return saved;
    }

    public List<RegistrationRequest> getAllPendingRequests() {
        return registrationRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    public List<RegistrationRequest> getRequestsByStatus(RequestStatus status) {
        return registrationRequestRepository.findByStatus(status);
    }

    public Optional<RegistrationRequest> findById(Long id) {
        return registrationRequestRepository.findById(id);
    }

    @Transactional
    public void approveRequest(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already processed");
        }

        // Generate password
        String generatedPassword = generatePassword();

        // Create User with generated password
        User user = new User(request.getEmail(), generatedPassword, request.getRequestedRole());
        user = userService.createUser(user);

        // Create Doctor or Patient based on role
        if (request.getRequestedRole() == Role.DOCTOR) {
            doctorService.createDoctorFromBasicInfo(
                    request.getFullName(),
                    request.getMobileNumber(),
                    user
            );
        } else if (request.getRequestedRole() == Role.PATIENT) {
            patientService.createPatientFromBasicInfo(
                    request.getFullName(),
                    request.getMobileNumber(),
                    user
            );
        }

        // Send password via email
        emailService.sendPasswordEmail(request.getEmail(), request.getFullName(), generatedPassword);

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        registrationRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already processed");
        }

        request.setStatus(RequestStatus.REJECTED);
        registrationRequestRepository.save(request);

        // Send rejection email
        emailService.sendRejectionEmail(request.getEmail(), request.getFullName(), null);
    }

    // Private helper method for password generation
    private String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        // Ensure at least one character from each category
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the rest randomly
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
    public Optional<RegistrationRequest> findByEmail(String email) {
        return registrationRequestRepository.findByEmail(email);
    }

    public List<RegistrationRequest> getAllRequests() {
        return registrationRequestRepository.findAll();
    }
}