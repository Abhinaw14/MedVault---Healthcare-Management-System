package com.medvault.demo.service;

import com.medvault.demo.entity.Role;
import com.medvault.demo.entity.User;
import com.medvault.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }

    @Transactional
    public User registerAdmin(String fullName, String contactNumber, String username, String password) {
        if (adminExists()) {
            throw new RuntimeException("Admin already exists. Only one admin is allowed.");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists.");
        }

        // Create admin user with additional fields
        User admin = new User(username, password, Role.ADMIN, fullName, contactNumber);
        admin.setPassword(passwordEncoder.encode(password));

        return userRepository.save(admin);
    }

    public Optional<User> findAdminUser() {
        return userRepository.findByRole(Role.ADMIN);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}