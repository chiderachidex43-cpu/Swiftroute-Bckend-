
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
import java.util.HashMap;
import java.util.Map;
import com.swiftroute.backend.model.AccountStatus;
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
        System.err.println("LOGIN MATCH COUNT: " + matchingUsers.size());

        // Refuse ambiguous accounts rather than accidentally
        // authenticating the wrong user.
        if (matchingUsers.size() != 1) {
            throw new RuntimeException("Invalid login details");
        }

        User user = matchingUsers.get(0);

        // Compare the supplied password against the BCrypt hash.
        boolean passwordMatches = passwordEncoder.matches(
        request.getPassword(),
        user.getPasswordHash()
);

System.err.println("LOGIN PASSWORD MATCH: " + passwordMatches);

if (!passwordMatches) {
    throw new RuntimeException("Invalid login details");
}
                if (user.getAccountStatus()
                != com.swiftroute.backend.model.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is suspended");
        }



        return user;
    }
    @Transactional
public Map<String, Object> verifyPaymentPassword(
        String email,
        String password) {

    var user = userRepository.findByEmailForUpdate(email)
            .orElseThrow(() ->
                    new RuntimeException("Account not found"));

    Map<String, Object> result = new HashMap<>();
    long now = System.currentTimeMillis();
    int maxAttempts = 5;
    long lockDuration = 60 * 60 * 1000L;

    if (user.getAccountStatus() != AccountStatus.ACTIVE) {
        throw new RuntimeException("Account is not active");
    }

    Long lockedUntil = user.getPaymentLockedUntil();

    if (lockedUntil != null && lockedUntil > now) {
        result.put("success", false);
        result.put("attemptsRemaining", 0);
        result.put("lockedUntil", lockedUntil);
        return result;
    }

    // Reset an expired lock.
    if (lockedUntil != null && lockedUntil <= now) {
        user.setPaymentFailedAttempts(0);
        user.setPaymentLockedUntil(null);
    }

    // Check the password against the stored BCrypt hash.
    if (password != null
            && !password.isBlank()
            && passwordEncoder.matches(
                    password,
                    user.getPasswordHash())) {

        user.setPaymentFailedAttempts(0);
        user.setPaymentLockedUntil(null);

        result.put("success", true);
        result.put("attemptsRemaining", maxAttempts);
        result.put("lockedUntil", null);
        return result;
    }

    // Record an incorrect password.
    int attempts = user.getPaymentFailedAttempts() + 1;
    user.setPaymentFailedAttempts(
            Math.min(attempts, maxAttempts));

    if (attempts >= maxAttempts) {
        long newLockedUntil = now + lockDuration;

        user.setPaymentLockedUntil(newLockedUntil);

        result.put("success", false);
        result.put("attemptsRemaining", 0);
        result.put("lockedUntil", newLockedUntil);
    } else {
        result.put("success", false);
        result.put("attemptsRemaining", maxAttempts - attempts);
        result.put("lockedUntil", null);
    }

    return result;
}
}
