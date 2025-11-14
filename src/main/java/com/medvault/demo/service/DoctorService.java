package com.medvault.demo.service;

import com.medvault.demo.entity.Doctor;
import com.medvault.demo.entity.User;
import com.medvault.demo.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor createDoctorFromBasicInfo(String fullName, String contactNumber, User user) {
        Doctor doctor = new Doctor();
        doctor.setFullName(fullName);
        doctor.setContactNumber(contactNumber);
        doctor.setUser(user);
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor updateDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
}