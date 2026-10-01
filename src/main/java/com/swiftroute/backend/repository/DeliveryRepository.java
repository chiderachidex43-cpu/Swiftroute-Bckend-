package com.swiftroute.backend.repository;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCustomer(User customer);

    List<Delivery> findByRider(User rider);

    List<Delivery> findByStatus(DeliveryStatus status);
}