package com.swiftroute.backend.service;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            UserRepository userRepository) {

        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    public Delivery createDelivery(
            Long customerId,
            String pickupLocation,
            String destination,
            String packageDetails,
            String vehicleType,
            BigDecimal amount) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException(
                    "Only customers can create deliveries");
        }

        Delivery delivery = new Delivery();

        delivery.setCustomer(customer);
        delivery.setPickupLocation(pickupLocation);
        delivery.setDestination(destination);
        delivery.setPackageDetails(packageDetails);
        delivery.setVehicleType(vehicleType);
        delivery.setAmount(amount);
        delivery.setStatus(DeliveryStatus.REQUESTED);

        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getCustomerDeliveries(String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        return deliveryRepository.findByCustomer(customer);
    }

    public List<Delivery> getAvailableDeliveries() {

    return deliveryRepository.findByStatus(
            DeliveryStatus.REQUESTED
    );
}
}