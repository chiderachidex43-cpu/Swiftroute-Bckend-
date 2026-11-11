package com.swiftroute.backend.repository;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCustomer(User customer);

    List<Delivery> findByRider(User rider);

    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    long countByTrackingNumberStartingWith(String prefix);

    List<Delivery> findByStatus(DeliveryStatus status);

    long countByStatus(DeliveryStatus status);

long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        java.time.LocalDateTime start,
        java.time.LocalDateTime end
);

List<Delivery> findTop6ByOrderByCreatedAtDesc();

}