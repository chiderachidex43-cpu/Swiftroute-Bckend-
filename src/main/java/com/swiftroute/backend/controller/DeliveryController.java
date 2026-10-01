package com.swiftroute.backend.controller;

import com.swiftroute.backend.dto.DeliveryRequest;
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
    public ResponseEntity<Delivery> createDelivery(
            @RequestBody DeliveryRequest request) {

        Delivery delivery = deliveryService.createDelivery(
                request.getCustomerId(),
                request.getPickupLocation(),
                request.getDestination(),
                request.getPackageDetails(),
                request.getVehicleType(),
                request.getAmount()
        );

        return ResponseEntity.ok(delivery);
    }

    @GetMapping
    public ResponseEntity<List<Delivery>> getCustomerDeliveries(
            Principal principal) {

        List<Delivery> deliveries =
                deliveryService.getCustomerDeliveries(
                        principal.getName()
                );

        return ResponseEntity.ok(deliveries);
    }
}