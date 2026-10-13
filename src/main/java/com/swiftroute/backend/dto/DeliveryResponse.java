package com.swiftroute.backend.dto;

import com.swiftroute.backend.model.Delivery;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long customerId,
        String customerName,
        Long riderId,
        String riderName,
        String pickupLocation,
        String destination,
        String packageDetails,
        String vehicleType,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getCustomer().getId(),
                delivery.getCustomer().getName(),
                delivery.getRider() != null
                        ? delivery.getRider().getId()
                        : null,
                delivery.getRider() != null
                        ? delivery.getRider().getName()
                        : null,
                delivery.getPickupLocation(),
                delivery.getDestination(),
                delivery.getPackageDetails(),
                delivery.getVehicleType(),
                delivery.getAmount(),
                delivery.getStatus().name(),
                delivery.getCreatedAt()
        );
    }
}