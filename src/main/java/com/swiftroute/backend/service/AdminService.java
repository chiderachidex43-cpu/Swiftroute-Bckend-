package com.swiftroute.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createAdmin(
            String name,
            String email,
            String phone,
            String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User admin = new User();

        admin.setName(name);
        admin.setEmail(email);
        admin.setPhone(phone);

        String hashedPassword =
                passwordEncoder.encode(password);

        admin.setPasswordHash(hashedPassword);

        admin.setRole(Role.ADMIN);

        return userRepository.save(admin);
    }
}