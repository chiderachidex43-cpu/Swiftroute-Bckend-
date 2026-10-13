
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
    private String accountStatus;
    private String message;

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
        this.accountStatus = "ACTIVE";
        this.message = "Login successful";
    }

    public LoginResponse(
            Long id,
            String name,
            String email,
            String phone,
            Role role,
            String accountStatus,
            String message,
            boolean pendingApproval){

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.accountStatus = accountStatus;
        this.message = message;
        this.accessToken = null;
        this.refreshToken = null;
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

    public String getAccountStatus() {
        return accountStatus;
    }

    public String getMessage() {
        return message;
    }
}
