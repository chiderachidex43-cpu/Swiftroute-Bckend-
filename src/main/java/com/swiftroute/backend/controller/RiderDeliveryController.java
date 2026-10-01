package com.swiftroute.backend.controller;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rider/deliveries")
public class RiderDeliveryController {

    private final DeliveryService deliveryService;

    public RiderDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<Delivery>> getAvailableDeliveries() {

        List<Delivery> deliveries =
                deliveryService.getAvailableDeliveries();

        return ResponseEntity.ok(deliveries);
    }
}