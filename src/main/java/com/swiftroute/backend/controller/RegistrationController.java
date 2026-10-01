package com.swiftroute.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.swiftroute.backend.dto.RegistrationResponse;
import com.swiftroute.backend.dto.LoginRequest;
import com.swiftroute.backend.dto.LoginResponse;
import com.swiftroute.backend.security.JwtService;
import com.swiftroute.backend.dto.RegistrationRequest;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.service.RegistrationService;


@RestController
@RequestMapping("/api/auth")
public class RegistrationController {
      private final RegistrationService registrationService;
     private final JwtService jwtService;
 public RegistrationController(
        RegistrationService registrationService,
        JwtService jwtService) {

    this.registrationService = registrationService;
    this.jwtService = jwtService;
}
  @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody RegistrationRequest request) {

        User user = registrationService.register(request);

        return ResponseEntity.ok(new RegistrationResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole()
        ));
    }
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request) {

    User user = registrationService.login(request);

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
        }
    }