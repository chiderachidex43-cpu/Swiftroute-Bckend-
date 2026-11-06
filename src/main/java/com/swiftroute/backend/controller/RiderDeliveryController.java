package com.swiftroute.backend.controller;

import com.swiftroute.backend.dto.DeliveryResponse;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.swiftroute.backend.model.DeliveryMedia;
import com.swiftroute.backend.service.DeliveryMediaService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rider/deliveries")
public class RiderDeliveryController {

    private final DeliveryService deliveryService;
private final DeliveryMediaService deliveryMediaService;

    public RiderDeliveryController(DeliveryService deliveryService, DeliveryMediaService deliveryMediaService) {
        this.deliveryService = deliveryService;
        this.deliveryMediaService = deliveryMediaService;
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
    @PutMapping("/{id}/pickup")
public ResponseEntity<?> markAsPickedUp(
        @PathVariable Long id,
        Principal principal) {

    try {
        Delivery delivery = deliveryService.markAsPickedUp(
                id,
                principal.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Delivery marked as picked up",
                        "deliveryId", delivery.getId(),
                        "status", delivery.getStatus().name()
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}
    // Update the logged-in rider's GPS location for an assigned delivery
@PutMapping("/{id}/location")
public ResponseEntity<?> updateRiderLocation(
        @PathVariable Long id,
        @RequestBody Map<String, Double> coordinates,
        Principal principal) {

    try {
        Double latitude = coordinates.get("latitude");
        Double longitude = coordinates.get("longitude");

        Delivery delivery = deliveryService.updateRiderLocation(
                id,
                principal.getName(),
                latitude,
                longitude
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rider location updated successfully");
        response.put("deliveryId", delivery.getId());
        response.put("latitude", delivery.getRiderLatitude());
        response.put("longitude", delivery.getRiderLongitude());
        response.put("updatedAt", delivery.getRiderLocationUpdatedAt());

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

@PostMapping(
        value = "/{id}/proof",
        consumes = "multipart/form-data"
)
public ResponseEntity<?> uploadDeliveryProof(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file,
        Principal principal) {

    try {
        DeliveryMedia media = deliveryMediaService.uploadDeliveryProof(
                id,
                principal.getName(),
                file
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Delivery proof uploaded successfully.",
                        "mediaId", media.getId(),
                        "fileName", media.getFileName(),
                        "contentType", media.getContentType()
                )
        );

    } catch (IOException | RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message",
                        e.getMessage() == null
                                ? "Unable to upload delivery proof"
                                : e.getMessage()
                ));
    }
}

@PostMapping("/{id}/verify-otp")
public ResponseEntity<?> verifyDeliveryOtp(
        @PathVariable Long id,
        @RequestBody Map<String, String> request,
        Principal principal) {

    try {
        String otp = request.get("otp");

        deliveryService.verifyDeliveryOtp(
                id,
                principal.getName(),
                otp
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Receiver verification successful."
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}


@PutMapping("/{id}/complete")
public ResponseEntity<?> completeDelivery(
        @PathVariable Long id,
        Principal principal) {

    try {
        Delivery delivery = deliveryService.markAsDelivered(
                id,
                principal.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Delivery completed successfully.",
                        "deliveryId", delivery.getId(),
                        "status", delivery.getStatus().name()
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

@PostMapping("/{id}/request-otp")
public ResponseEntity<?> requestDeliveryOtp(
        @PathVariable Long id,
        Principal principal) {

    try {
        deliveryService.requestDeliveryOtp(
                id,
                principal.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Verification code has been sent to the receiver's email."
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

}