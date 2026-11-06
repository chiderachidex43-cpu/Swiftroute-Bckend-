
package com.swiftroute.backend.dto;

import com.swiftroute.backend.model.Delivery;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        String trackingNumber,
        Long customerId,
        String customerName,
        Long riderId,
        String riderName,
        String pickupLocation,
        String pickupCountryCode,
        String destination,
        String packageDetails,
        String vehicleType,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt,

        String receiver,
        String receiverPhone,
        String receiverEmail,
        String goodsName,
        Integer quantity,
        String note,
        String speed,

        Double pickupLatitude,
        Double pickupLongitude,
        Double destinationLatitude,
        Double destinationLongitude,

        Double riderLatitude,
        Double riderLongitude,
        LocalDateTime riderLocationUpdatedAt
) {
    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getTrackingNumber(),
                delivery.getCustomer().getId(),
                delivery.getCustomer().getName(),
                delivery.getRider() != null
                        ? delivery.getRider().getId()
                        : null,
                delivery.getRider() != null
                        ? delivery.getRider().getName()
                        : null,
                delivery.getPickupLocation(),
                delivery.getPickupCountryCode(),
                delivery.getDestination(),
                delivery.getPackageDetails(),
                delivery.getVehicleType(),
                delivery.getAmount(),
                delivery.getStatus().name(),
                delivery.getCreatedAt(),

                delivery.getReceiver(),
                delivery.getReceiverPhone(),
                delivery.getReceiverEmail(),
                delivery.getGoodsName(),
                delivery.getQuantity(),
                delivery.getNote(),
                delivery.getSpeed(),

                delivery.getPickupLatitude(),
                delivery.getPickupLongitude(),
                delivery.getDestinationLatitude(),
                delivery.getDestinationLongitude(),

                delivery.getRiderLatitude(),
                delivery.getRiderLongitude(),
                delivery.getRiderLocationUpdatedAt()
        );
    }
}
