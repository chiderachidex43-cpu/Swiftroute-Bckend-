package com.swiftroute.backend.controller;

import com.swiftroute.backend.dto.DeliveryResponse;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rider/deliveries")
public class RiderDeliveryController {

    private final DeliveryService deliveryService;

    public RiderDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // View available deliveries
    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAvailableDeliveries() {

        List<DeliveryResponse> deliveries =
                deliveryService.getAvailableDeliveries()
                        .stream()
                        .map(DeliveryResponse::from)
                        .toList();

        return ResponseEntity.ok(deliveries);
    }

    // View deliveries assigned to the logged-in rider
    @GetMapping("/assigned")
    public ResponseEntity<List<DeliveryResponse>> getAssignedDeliveries(
            Principal principal) {

        List<DeliveryResponse> deliveries =
                deliveryService.getRiderDeliveries(
                                principal.getName())
                        .stream()
                        .map(DeliveryResponse::from)
                        .toList();

        return ResponseEntity.ok(deliveries);
    }

    // Accept an available delivery
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(
            @PathVariable Long id,
            Principal principal) {

        try {
            Delivery delivery = deliveryService.acceptDelivery(
                    id,
                    principal.getName()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Delivery accepted successfully",
                            "deliveryId", delivery.getId(),
                         "status", delivery.getStatus().name(),
                            "riderId", delivery.getRider().getId()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}