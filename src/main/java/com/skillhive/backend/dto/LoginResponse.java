package com.skillhive.backend.dto;

public class LoginResponse {

    private String token;
    private Long id;
    private String fullName;
    private String email;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long id, String fullName,
                         String email, String role) {
        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
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

    public String getRole() {
        return role;
    }
}