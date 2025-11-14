package com.medvault.demo.service;

import com.medvault.demo.entity.Patient;
import com.medvault.demo.entity.User;
import com.medvault.demo.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient createPatientFromBasicInfo(String fullName, String contactNumber, User user) {
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setContactNumber(contactNumber);
        patient.setUser(user);
        return patientRepository.save(patient);
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }

    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient updatePatient(Patient patient) {
        return patientRepository.save(patient);
    }
}