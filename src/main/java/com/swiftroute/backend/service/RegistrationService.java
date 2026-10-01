
package com.swiftroute.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public User register(RegistrationRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPhone(request.getPhone());

        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        user.setPasswordHash(hashedPassword);

       user.setRole(Role.CUSTOMER);
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {

        String identifier = request.getIdentifier().trim();

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByName(identifier))
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid login details"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException(
                    "Invalid login details");
        }

        return user;
    }
}
