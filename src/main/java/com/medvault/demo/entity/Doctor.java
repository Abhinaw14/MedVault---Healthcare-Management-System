package com.medvault.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String specialization;
    private String contactNumber;
    private String qualification;
    private int yearsOfExperience;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Constructors
    public Doctor() {}

    public Doctor(String fullName, String specialization, String contactNumber, String qualification, int yearsOfExperience, User user) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.qualification = qualification;
        this.yearsOfExperience = yearsOfExperience;
        this.user = user;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

