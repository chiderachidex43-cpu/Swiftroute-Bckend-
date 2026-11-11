
package com.swiftroute.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swiftroute.backend.dto.AdminCreationRequest;
import com.swiftroute.backend.dto.AdminDashboardResponse;
import com.swiftroute.backend.dto.DeliveryResponse;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.service.AdminDashboardService;
import com.swiftroute.backend.service.AdminService;
import com.swiftroute.backend.service.DeliveryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;
    private final DeliveryService deliveryService;
    private final AdminDashboardService adminDashboardService;

    public AdminController(
            AdminService adminService,
            DeliveryService deliveryService,
            AdminDashboardService adminDashboardService) {

        this.adminService = adminService;
        this.deliveryService = deliveryService;
        this.adminDashboardService = adminDashboardService;
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

            return ResponseEntity.ok(Map.of(
                    "message", "Admin account created successfully",
                    "id", admin.getId(),
                    "name", admin.getName(),
                    "email", admin.getEmail(),
                    "role", admin.getRole()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // List riders waiting for admin approval
    @GetMapping("/pending-riders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingRiders() {

        List<Map<String, Object>> riders =
                adminService.getPendingRiders()
                        .stream()
                        .map(rider -> {
                            Map<String, Object> response = new HashMap<>();

                            response.put("id", rider.getId());
                            response.put("name", rider.getName());
                            response.put("email", rider.getEmail());
                            response.put("phone", rider.getPhone());
                            response.put("role", rider.getRole());
                            response.put(
                                    "riderApproved",
                                    rider.isRiderApproved()
                            );

                            return response;
                        })
                        .toList();

        return ResponseEntity.ok(riders);
    }

    // Approve a rider
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRider(@PathVariable Long id) {

        try {
            User rider = adminService.approveRider(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Rider approved successfully",
                    "id", rider.getId(),
                    "name", rider.getName(),
                    "email", rider.getEmail(),
                    "role", rider.getRole(),
                    "riderApproved", rider.isRiderApproved()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Assign an approved rider to a delivery
    @PutMapping("/deliveries/{id}/assign/{riderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRider(
            @PathVariable Long id,
            @PathVariable Long riderId) {

        try {
            Delivery delivery =
                    deliveryService.assignRider(id, riderId);

            return ResponseEntity.ok(Map.of(
                    "message", "Rider assigned successfully",
                    "deliveryId", delivery.getId(),
                    "riderId", delivery.getRider().getId(),
                    "status", delivery.getStatus().name()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Retrieve all deliveries
    @GetMapping("/deliveries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {

        List<DeliveryResponse> deliveries =
                deliveryService.getAllDeliveries()
                        .stream()
                        .map(DeliveryResponse::from)
                        .toList();

        return ResponseEntity.ok(deliveries);
    }

    // Retrieve real admin dashboard statistics
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {

        AdminDashboardResponse dashboard =
                adminDashboardService.getDashboard();

        return ResponseEntity.ok(dashboard);
    }
}
