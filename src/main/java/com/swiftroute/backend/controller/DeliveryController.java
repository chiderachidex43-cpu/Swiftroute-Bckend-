package com.swiftroute.backend.controller;

import com.swiftroute.backend.dto.DeliveryRequest;
import com.swiftroute.backend.dto.DeliveryResponse;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @RequestBody DeliveryRequest request,
            Principal principal) {

        Delivery delivery = deliveryService.createDelivery(
                principal.getName(),
                request.getPickupLocation(),
                request.getDestination(),
                request.getPackageDetails(),
                request.getVehicleType(),
                request.getAmount()
        );

        return ResponseEntity.ok(DeliveryResponse.from(delivery));
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getCustomerDeliveries(
            Principal principal) {

        List<DeliveryResponse> deliveries =
                deliveryService.getCustomerDeliveries(
                        principal.getName()
                ).stream()
                        .map(DeliveryResponse::from)
                        .toList();

        return ResponseEntity.ok(deliveries);

    }


@PutMapping("/{id}/cancel")
public ResponseEntity<?> cancelDelivery(
        @PathVariable Long id,
        Principal principal) {

    try {
        Delivery delivery = deliveryService.cancelDelivery(
                id,
                principal.getName()
        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Delivery cancelled successfully",
                        "deliveryId", delivery.getId(),
                        "status", delivery.getStatus().name()
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(java.util.Map.of(
                        "message", e.getMessage()
                ));
    }
}

}
