package com.medvault.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PatientProfileUpdateDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Min(value = 1, message = "Age must be at least 1")
    private Integer age;

    private String gender;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    private String address;

    public PatientProfileUpdateDTO() {}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

