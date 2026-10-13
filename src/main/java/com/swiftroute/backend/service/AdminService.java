
package com.swiftroute.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone number already exists");
        }

        User admin = new User();

        admin.setName(name);
        admin.setEmail(email);
        admin.setPhone(phone);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setRiderApproved(true);

        return userRepository.save(admin);
    }

    public List<User> getPendingRiders() {
        return userRepository.findAllByRoleAndRiderApprovedFalse(
                Role.RIDER
        );
    }

    @Transactional
    public User approveRider(Long id) {

        User rider = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found"));

        if (rider.getRole() != Role.RIDER) {
            throw new RuntimeException(
                    "This user is not a rider"
            );
        }

        if (rider.isRiderApproved()) {
            throw new RuntimeException(
                    "Rider is already approved"
            );
        }

        rider.setRiderApproved(true);

        return userRepository.save(rider);
    }
}
