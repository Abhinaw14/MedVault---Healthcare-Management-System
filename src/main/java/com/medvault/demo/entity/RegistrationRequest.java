package com.medvault.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_requests")
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    private Role requestedRole;  // DOCTOR or PATIENT

    @Enumerated(EnumType.STRING)
    private RequestStatus status;  // PENDING, APPROVED, REJECTED

    // Constructors
    public RegistrationRequest() {
        this.status = RequestStatus.PENDING;
    }

    public RegistrationRequest(String fullName, String email, String mobileNumber, Role requestedRole) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.requestedRole = requestedRole;
        this.status = RequestStatus.PENDING;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Role getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(Role requestedRole) {
        this.requestedRole = requestedRole;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}