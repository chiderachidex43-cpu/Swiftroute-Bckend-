
package com.swiftroute.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swiftroute.backend.dto.AdminCreationRequest;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Create an admin account
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdmin(
            @RequestBody AdminCreationRequest request) {

        try {
            User admin = adminService.createAdmin(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Admin account created successfully",
                            "id", admin.getId(),
                            "name", admin.getName(),
                            "email", admin.getEmail(),
                            "role", admin.getRole()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // List riders waiting for admin approval
    @GetMapping("/pending-riders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingRiders() {

        List<Map<String, Object>> riders = adminService.getPendingRiders()
                .stream()
                .map(rider -> {
                    Map<String, Object> response = new HashMap<>();

                    response.put("id", rider.getId());
                    response.put("name", rider.getName());
                    response.put("email", rider.getEmail());
                    response.put("phone", rider.getPhone());
                    response.put("role", rider.getRole());
                    response.put("riderApproved", rider.isRiderApproved());

                    return response;
                })
                .toList();

        return ResponseEntity.ok(riders);
    }

    // Approve a rider
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRider(
            @PathVariable Long id) {

        try {
            User rider = adminService.approveRider(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Rider approved successfully",
                            "id", rider.getId(),
                            "name", rider.getName(),
                            "email", rider.getEmail(),
                            "role", rider.getRole(),
                            "riderApproved", rider.isRiderApproved()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}