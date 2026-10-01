package com.swiftroute.backend.dto;

import com.swiftroute.backend.model.Role;

public class RegistrationResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;

    public RegistrationResponse(
            Long id,
            String name,
            String email,
            String phone,
            Role role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
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
}