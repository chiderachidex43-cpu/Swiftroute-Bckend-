
package com.swiftroute.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.OperatingRegion;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.OperatingRegionRepository;
import com.swiftroute.backend.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private static final DateTimeFormatter TRACKING_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyMMdd");

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OperatingRegionRepository operatingRegionRepository;
    private final EntityManager entityManager;
    private final DeliveryOtpService deliveryOtpService;
    private final DeliveryMediaService deliveryMediaService;
    private final EmailService emailService;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            UserRepository userRepository,
            OperatingRegionRepository operatingRegionRepository,
            EntityManager entityManager,
            DeliveryOtpService deliveryOtpService,
            DeliveryMediaService deliveryMediaService,
            EmailService emailService) {

        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
        this.operatingRegionRepository = operatingRegionRepository;
        this.entityManager = entityManager;
        this.deliveryOtpService = deliveryOtpService;
        this.deliveryMediaService = deliveryMediaService;
        this.emailService = emailService;
    }

    @Transactional
    public Delivery createDelivery(
            String email,
            String pickupLocation,
            String pickupCountryCode,
            String destination,
            String packageDetails,
            String vehicleType,
            BigDecimal amount,
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
                Double destinationLongitude) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (customer.getRole() == null
                || !customer.getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException(
                    "Only customers can create deliveries");
        }

        if (pickupLocation == null || pickupLocation.isBlank()) {
            throw new IllegalArgumentException(
                    "Pickup location is required");
        }

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination is required");
        }

        if (pickupCountryCode == null
                || !pickupCountryCode.matches("[A-Za-z]{2}")) {
            throw new IllegalArgumentException(
                    "A valid two-letter pickup country code is required");
        }

        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle type is required");
        }

        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "A valid delivery amount is required");
        }

        final String countryCode =
        pickupCountryCode.toUpperCase(Locale.ROOT);

        // Find the active operating region for the pickup country.
        List<OperatingRegion> matchingRegions =
                operatingRegionRepository.findByActiveTrue()
                        .stream()
                        .filter(region ->
                                region.getCountryCode() != null
                                && region.getCountryCode()
                                        .equalsIgnoreCase(countryCode))
                        .toList();

        if (matchingRegions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No active operating region exists for country "
                            + countryCode);
        }

        if (matchingRegions.size() > 1) {
            throw new IllegalArgumentException(
                    "Multiple active operating regions use country "
                            + countryCode
                            + ". A specific operating region must be selected.");
        }

        OperatingRegion region = matchingRegions.get(0);

        ZoneId regionZone;
        try {
            regionZone = ZoneId.of(region.getTimeZone());
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "The operating region has an invalid time zone",
                    exception);
        }

        String datePart = LocalDate.now(regionZone)
                .format(TRACKING_DATE_FORMAT);

        String trackingPrefix =
                "SR-" + countryCode + "-" + datePart + "-";

        /*
         * Lock the tracking prefix inside the current PostgreSQL
         * transaction. Requests generating a number for the same
         * country and date must take this lock one at a time.
         */
        entityManager.createNativeQuery(
                        "SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, trackingPrefix)
                .getSingleResult();

        long nextSequence =
                deliveryRepository
                        .countByTrackingNumberStartingWith(trackingPrefix)
                        + 1;

        String trackingNumber;

        do {
            if (nextSequence > 9999) {
                throw new IllegalStateException(
                        "Daily tracking-number limit reached for "
                                + trackingPrefix);
            }

            trackingNumber = trackingPrefix
                    + String.format(Locale.ROOT, "%04d", nextSequence);

            nextSequence++;
        } while (deliveryRepository
                .findByTrackingNumber(trackingNumber)
                .isPresent());

        Delivery delivery = new Delivery();

        delivery.setCustomer(customer);
        delivery.setPickupLocation(pickupLocation.trim());
        delivery.setPickupCountryCode(countryCode);
        delivery.setDestination(destination.trim());
        delivery.setPackageDetails(packageDetails);
        delivery.setVehicleType(vehicleType);
        delivery.setAmount(amount);
        delivery.setReceiver(receiver);
        delivery.setReceiverPhone(receiverPhone);
        delivery.setReceiverEmail(receiverEmail);
        delivery.setGoodsName(goodsName);
        delivery.setQuantity(quantity);
        delivery.setNote(note);
        delivery.setSpeed(speed);

        delivery.setPickupLatitude(pickupLatitude);
        delivery.setPickupLongitude(pickupLongitude);
        delivery.setDestinationLatitude(destinationLatitude);
        delivery.setDestinationLongitude(destinationLongitude);
        delivery.setTrackingNumber(trackingNumber);
        delivery.setStatus(DeliveryStatus.REQUESTED);

        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getCustomerDeliveries(String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (customer.getRole() == null
                || !customer.getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException(
                    "Only customers can view their deliveries");
        }

        return deliveryRepository.findByCustomer(customer);
    }

    @Transactional
    public Delivery cancelDelivery(
            Long deliveryId,
            String customerEmail) {

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (customer.getRole() == null
                || !customer.getRole().name().equals("CUSTOMER")) {
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

    public List<Delivery> getAllDeliveries() {
    return deliveryRepository.findAll();
}

    public List<Delivery> getRiderDeliveries(String riderEmail) {

        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found"));

        if (rider.getRole() == null
                || !rider.getRole().name().equals("RIDER")) {
            throw new RuntimeException(
                    "Only riders can view their assigned deliveries");
        }

        return deliveryRepository.findByRider(rider);
    }

    @Transactional
    public Delivery acceptDelivery(
            Long deliveryId,
            String riderEmail) {

        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() ->
                        new RuntimeException("Rider not found"));

        if (rider.getRole() == null
                || !rider.getRole().name().equals("RIDER")) {
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
    
   

    @Transactional
public Delivery updateRiderLocation(
        Long deliveryId,
        String riderEmail,
        Double latitude,
        Double longitude) {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() == null
            || !rider.getRole().name().equals("RIDER")) {
        throw new RuntimeException(
                "Only riders can update their location");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved to share location");
    }

    if (latitude == null
            || !Double.isFinite(latitude)
            || latitude < -90
            || latitude > 90) {
        throw new IllegalArgumentException(
                "Invalid latitude");
    }

    if (longitude == null
            || !Double.isFinite(longitude)
            || longitude < -180
            || longitude > 180) {
        throw new IllegalArgumentException(
                "Invalid longitude");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not assigned to this delivery");
    }

    DeliveryStatus status = delivery.getStatus();

    if (status != DeliveryStatus.ACCEPTED
            && status != DeliveryStatus.PICKED_UP
            && status != DeliveryStatus.IN_TRANSIT) {
        throw new RuntimeException(
                "Location updates are not allowed for this delivery status");
    }

    delivery.setRiderLatitude(latitude);
    delivery.setRiderLongitude(longitude);
    delivery.setRiderLocationUpdatedAt(
            java.time.LocalDateTime.now());

    return deliveryRepository.save(delivery);
}
public Delivery getDeliveryTracking(
        Long deliveryId,
        String customerEmail) {

    User customer = userRepository.findByEmail(customerEmail)
            .orElseThrow(() ->
                    new RuntimeException("Customer not found"));

    if (customer.getRole() == null
            || !customer.getRole().name().equals("CUSTOMER")) {
        throw new RuntimeException(
                "Only customers can view delivery tracking");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (!delivery.getCustomer().getId().equals(customer.getId())) {
        throw new RuntimeException(
                "You cannot track another customer's delivery");
    }

    return delivery;
}

@Transactional
public Delivery markAsPickedUp(Long deliveryId, String riderEmail) {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() == null
            || !rider.getRole().name().equals("RIDER")) {
        throw new RuntimeException(
                "Only riders can pick up deliveries");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved to pick up deliveries");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not assigned to this delivery");
    }

    if (delivery.getStatus() != DeliveryStatus.ACCEPTED) {
        throw new RuntimeException(
                "Only accepted deliveries can be picked up");
    }

    delivery.setStatus(DeliveryStatus.PICKED_UP);

    return deliveryRepository.save(delivery);
}

@Transactional
public Delivery assignRider(Long deliveryId, Long riderId) {

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    User rider = userRepository.findById(riderId)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() != Role.RIDER) {
        throw new RuntimeException("Only riders can be assigned");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved before assignment");
    }

    if (delivery.getRider() != null) {
        throw new RuntimeException(
                "This delivery already has an assigned rider");
    }

    if (delivery.getStatus() != DeliveryStatus.REQUESTED
            && delivery.getStatus() != DeliveryStatus.ACCEPTED) {
        throw new RuntimeException(
                "Only requested or accepted deliveries can be assigned");
    }

    delivery.setRider(rider);
    delivery.setStatus(DeliveryStatus.ACCEPTED);

    return deliveryRepository.save(delivery);
}

@Transactional
public Delivery markAsDelivered(
        Long deliveryId,
        String riderEmail) {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() != Role.RIDER) {
        throw new RuntimeException(
                "Only riders can complete deliveries");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved to complete deliveries");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not assigned to this delivery");
    }

    if (delivery.getStatus() != DeliveryStatus.PICKED_UP
            && delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
        throw new RuntimeException(
                "This delivery cannot be completed in its current status");
    }

    // Require rider delivery-proof evidence.
    if (!deliveryMediaService.hasDeliveryProof(deliveryId)) {
        throw new RuntimeException(
                "Delivery proof photo is required before completion");
    }

    // Require successful receiver OTP verification.
    if (!deliveryOtpService.isOtpVerified(deliveryId)) {
        throw new RuntimeException(
                "Receiver OTP must be verified before completion");
    }

    delivery.setStatus(DeliveryStatus.DELIVERED);

    return deliveryRepository.save(delivery);
}


@Transactional
public void requestDeliveryOtp(Long deliveryId, String riderEmail) {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() != Role.RIDER) {
        throw new RuntimeException(
                "Only riders can request delivery verification");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not assigned to this delivery");
    }

    if (delivery.getStatus() != DeliveryStatus.PICKED_UP
            && delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
        throw new RuntimeException(
                "OTP can only be requested after pickup");
    }

    if (delivery.getReceiverEmail() == null
            || delivery.getReceiverEmail().isBlank()) {
        throw new RuntimeException(
                "Receiver email address is missing");
    }

    String otp = deliveryOtpService.generateAndSaveOtp(deliveryId);

    emailService.sendOtpEmail(
            delivery.getReceiverEmail(),
            delivery.getReceiver(),
            otp
    );
}

@Transactional
public void verifyDeliveryOtp(
        Long deliveryId,
        String riderEmail,
        String providedOtp) {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    if (rider.getRole() != Role.RIDER) {
        throw new RuntimeException(
                "Only riders can verify delivery OTPs");
    }

    if (!rider.isRiderApproved()) {
        throw new RuntimeException(
                "Rider must be approved");
    }

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not assigned to this delivery");
    }

    deliveryOtpService.verifyOtp(deliveryId, providedOtp);
}

}
