package com.medvault.demo.service;

import com.medvault.demo.entity.User;
import com.medvault.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user - encodes the password
     */
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Update an existing user - does NOT encode password (assumes already encoded)
     */
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Update password for an existing user - encodes the new password
     */
    @Transactional
    public User updatePassword(User user, String newPlainPassword) {
        user.setPassword(passwordEncoder.encode(newPlainPassword));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
