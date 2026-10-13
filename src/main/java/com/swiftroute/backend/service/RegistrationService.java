
package com.swiftroute.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swiftroute.backend.dto.LoginRequest;
import com.swiftroute.backend.dto.RegistrationRequest;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.UserRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationRequest request) {

        // Normalize email to prevent case-based duplicates.
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone = request.getPhone().trim();

        // Check email uniqueness safely.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Check phone uniqueness safely, even if old duplicate
        // records already exist in the database.
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone number already exists");
        }

        // Only CUSTOMER and RIDER can register publicly.
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        Role requestedRole;

        try {
            requestedRole = Role.valueOf(
                request.getRole().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException(
                "Invalid role. Only CUSTOMER or RIDER are allowed"
            );
        }

        if (requestedRole == Role.ADMIN) {
            throw new RuntimeException(
                "Admin registration is not allowed"
            );
        }

        if (requestedRole != Role.CUSTOMER
                && requestedRole != Role.RIDER) {
            throw new RuntimeException(
                "Invalid role. Only CUSTOMER or RIDER are allowed"
            );
        }

        // Create the user.
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPhone(phone);

        // Never store the plain-text password.
        user.setPasswordHash(
            passwordEncoder.encode(request.getPassword())
        );

        user.setRole(requestedRole);

        user.setAccountStatus(
            com.swiftroute.backend.model.AccountStatus.ACTIVE
        );

        user.setRiderApproved(
            requestedRole != Role.RIDER
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {

        if (request.getIdentifier() == null
                || request.getIdentifier().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new RuntimeException("Invalid login details");
        }

        String identifier = request.getIdentifier().trim();

        List<User> matchingUsers;

        // Email or phone login.
        if (identifier.contains("@")) {
            matchingUsers =
                userRepository.findAllByEmailIgnoreCase(
                    identifier.toLowerCase(Locale.ROOT)
                );
        } else {
            matchingUsers =
                userRepository.findAllByPhone(identifier);
        }

        // Refuse ambiguous accounts rather than accidentally
        // authenticating the wrong user.
        if (matchingUsers.size() != 1) {
            throw new RuntimeException("Invalid login details");
        }

        User user = matchingUsers.get(0);

        // Compare the supplied password against the BCrypt hash.
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {
            throw new RuntimeException("Invalid login details");
        }
                if (user.getAccountStatus()
                != com.swiftroute.backend.model.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is suspended");
        }



        return user;
    }
}
