package com.swiftroute.backend.repository;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryOtpRepository extends JpaRepository<DeliveryOtp, Long> {

    Optional<DeliveryOtp> findByDelivery(Delivery delivery);

    Optional<DeliveryOtp> findByDeliveryId(Long deliveryId);
}