package com.swiftroute.backend.service;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;

import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.UserRepository;
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public List<User> getAllUsers() {
    return userRepository.findAll();
}
}
