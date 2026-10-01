package com.swiftroute.backend.controller;

import com.swiftroute.backend.dto.AdminCreationRequest;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<User> createAdmin(
            @RequestBody AdminCreationRequest request) {

        User admin = adminService.createAdmin(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );

        return ResponseEntity.ok(admin);
    }
}