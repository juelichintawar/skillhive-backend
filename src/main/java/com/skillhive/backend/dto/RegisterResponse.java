package com.skillhive.backend.dto;

public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String college;
    private String role;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String fullName, String email,
                            String phone, String college, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCollege() {
        return college;
    }

    public String getRole() {
        return role;
    }
}