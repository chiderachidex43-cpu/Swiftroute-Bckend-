
package com.swiftroute.backend.service;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            String email,
            String pickupLocation,
            String destination,
            String packageDetails,
            String vehicleType,
            BigDecimal amount) {

        User customer = userRepository.findByEmail(email)
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

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException(
                    "Only customers can view their deliveries");
        }

        return deliveryRepository.findByCustomer(customer);
    }

    @Transactional
    public Delivery cancelDelivery(Long deliveryId, String customerEmail) {

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException(
                    "Only customers can cancel deliveries");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        if (!delivery.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException(
                    "You cannot cancel another customer's delivery");
        }

        if (delivery.getStatus() != DeliveryStatus.REQUESTED) {
            throw new RuntimeException(
                    "Only requested deliveries can be cancelled");
        }

        delivery.setStatus(DeliveryStatus.CANCELLED);
        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getAvailableDeliveries() {
        return deliveryRepository.findByStatus(
                DeliveryStatus.REQUESTED);
    }

    public List<Delivery> getRiderDeliveries(String riderEmail) {

        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found"));

        if (!rider.getRole().name().equals("RIDER")) {
            throw new RuntimeException(
                    "Only riders can view their assigned deliveries");
        }

        return deliveryRepository.findByRider(rider);
    }

    @Transactional
    public Delivery acceptDelivery(Long deliveryId, String riderEmail) {

        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found"));

        if (!rider.getRole().name().equals("RIDER")) {
            throw new RuntimeException(
                    "Only riders can accept deliveries");
        }

        if (!rider.isRiderApproved()) {
            throw new RuntimeException(
                    "Rider must be approved before accepting deliveries");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.REQUESTED
                || delivery.getRider() != null) {
            throw new RuntimeException(
                    "Delivery is no longer available");
        }

        delivery.setRider(rider);
        delivery.setStatus(DeliveryStatus.ACCEPTED);

        return deliveryRepository.save(delivery);
    }
}
