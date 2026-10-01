package com.swiftroute.backend.dto;

import com.swiftroute.backend.model.Role;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(
            Long id,
            String name,
            String email,
            String phone,
            Role role,
            String accessToken,
            String refreshToken) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}