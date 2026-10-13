
package com.swiftroute.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.swiftroute.backend.dto.LoginRequest;
import com.swiftroute.backend.dto.LoginResponse;
import com.swiftroute.backend.dto.RegistrationRequest;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.model.AccountStatus;
import com.swiftroute.backend.repository.UserRepository;
import com.swiftroute.backend.security.JwtService;
import com.swiftroute.backend.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public RegistrationController(
            RegistrationService registrationService,
            JwtService jwtService,
            UserRepository userRepository) {

        this.registrationService = registrationService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegistrationRequest request) {

        try {
            User user = registrationService.register(request);

            String message = user.getRole() == Role.RIDER
                    && !user.isRiderApproved()
                    ? "Registration successful. Your account is pending admin approval."
                    : "Registration successful.";

            return ResponseEntity.ok(
                    Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "phone", user.getPhone(),
                            "role", user.getRole(),
                            "accountStatus", user.getRole() == Role.RIDER
                                    && !user.isRiderApproved()
                                    ? "PENDING_APPROVAL" : "ACTIVE",
                            "message", message
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {
            User user = registrationService.login(request);

            // Pending riders receive HTTP 200, but no tokens.
            if (user.getRole() == Role.RIDER
                    && !user.isRiderApproved()) {

                return ResponseEntity.ok(
                        new LoginResponse(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getRole(),
                                "PENDING_APPROVAL",
                                "Your account is pending admin approval",
                                true
                        )
                );
            }

            // Only eligible users receive authentication tokens.
            String accessToken =
                    jwtService.generateAccessToken(user.getEmail());

            String refreshToken =
                    jwtService.generateRefreshToken(user.getEmail());

            return ResponseEntity.ok(
                    new LoginResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getRole(),
                            accessToken,
                            refreshToken
                    )
            );

        } catch (RuntimeException e) {
            String message = e.getMessage();

            if ("Account is suspended".equals(message)) {
                return ResponseEntity.status(403)
                        .body("Your account has been suspended");
            }

            return ResponseEntity.status(401)
                    .body("Invalid login details");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Refresh token is required");
        }

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401)
                    .body("Invalid or expired refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);

        var userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401)
                    .body("Account no longer exists");
        }

        User user = userOptional.get();

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            return ResponseEntity.status(401)
                    .body("Account is suspended");
        }

        if (user.getRole() == Role.RIDER
                && !user.isRiderApproved()) {
            return ResponseEntity.status(401)
                    .body("Rider account is not approved");
        }

        String newAccessToken =
                jwtService.generateAccessToken(email);

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }
}
