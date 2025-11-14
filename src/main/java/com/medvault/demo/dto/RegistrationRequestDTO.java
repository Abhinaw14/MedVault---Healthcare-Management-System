package com.medvault.demo.dto;

import com.medvault.demo.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistrationRequestDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotNull(message = "Requested role is required")
    private Role requestedRole;

    public RegistrationRequestDTO() {}

    public RegistrationRequestDTO(String fullName, String email, String mobileNumber, Role requestedRole) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.requestedRole = requestedRole;
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
}

