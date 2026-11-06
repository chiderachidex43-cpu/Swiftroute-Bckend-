package com.swiftroute.backend.service;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryOtp;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.repository.DeliveryOtpRepository;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class DeliveryOtpService {

    private final DeliveryOtpRepository otpRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateOtp() {
    int otp = secureRandom.nextInt(1_000_000);
    return String.format("%06d", otp);
}

    @Value("${delivery.otp.hash-secret}")
    private String otpHashSecret;

    @Value("${delivery.otp.expiration-minutes:5}")
    private long expirationMinutes;

    @Value("${delivery.otp.max-attempts:5}")
    private int maxAttempts;

    public DeliveryOtpService(
            DeliveryOtpRepository otpRepository,
            DeliveryRepository deliveryRepository,
            UserRepository userRepository) {

        this.otpRepository = otpRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    private String hashOtp(String otp) {
    try {
        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
                otpHashSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] hashedBytes = mac.doFinal(
                otp.getBytes(StandardCharsets.UTF_8)
        );

        return Base64.getEncoder().encodeToString(hashedBytes);

    } catch (Exception e) {
        throw new IllegalStateException("Failed to hash OTP", e);
    }
}
private boolean matchesOtp(String providedOtp, String storedHash) {
    String providedHash = hashOtp(providedOtp);

    return MessageDigest.isEqual(
            providedHash.getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8)
    );
}
private LocalDateTime calculateExpirationTime() {
    return LocalDateTime.now().plusMinutes(expirationMinutes);
}

private void validateDeliveryStatus(Delivery delivery) {

    if (delivery.getStatus() != DeliveryStatus.PICKED_UP
            && delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {

        throw new IllegalStateException(
                "OTP can only be generated for picked-up or in-transit deliveries."
        );
    }
}
private Delivery getDelivery(Long deliveryId) {

    return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Delivery not found with ID: " + deliveryId
            ));
}

@Transactional
public String generateAndSaveOtp(Long deliveryId) {

    Delivery delivery = getDelivery(deliveryId);

    validateDeliveryStatus(delivery);

    String otp = generateOtp();

    DeliveryOtp deliveryOtp = otpRepository
            .findByDeliveryId(deliveryId)
            .orElseGet(DeliveryOtp::new);

    deliveryOtp.setDelivery(delivery);
    deliveryOtp.setOtpHash(hashOtp(otp));
    deliveryOtp.setExpiresAt(calculateExpirationTime());
    deliveryOtp.setFailedAttempts(0);
    deliveryOtp.setUsed(false);

    otpRepository.save(deliveryOtp);

    return otp;
}
@Transactional(noRollbackFor = IllegalArgumentException.class)
public void verifyOtp(Long deliveryId, String providedOtp) {
    Delivery delivery = getDelivery(deliveryId);

    validateDeliveryStatus(delivery);

    DeliveryOtp deliveryOtp = otpRepository
            .findByDeliveryId(deliveryId)
            .orElseThrow(() -> new IllegalStateException(
                    "No OTP found for this delivery."
            ));

    if (deliveryOtp.isUsed()) {
        throw new IllegalStateException("This OTP has already been used.");
    }

    if (deliveryOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new IllegalStateException("OTP has expired.");
    }

    if (deliveryOtp.getFailedAttempts() >= maxAttempts) {
        throw new IllegalStateException(
                "Maximum OTP verification attempts exceeded."
        );
    }

    if (providedOtp == null || !providedOtp.matches("\\d{6}")) {
        deliveryOtp.setFailedAttempts(
                deliveryOtp.getFailedAttempts() + 1
        );

        otpRepository.save(deliveryOtp);

        throw new IllegalArgumentException("Invalid OTP.");
    }

    if (!matchesOtp(providedOtp, deliveryOtp.getOtpHash())) {

        deliveryOtp.setFailedAttempts(
                deliveryOtp.getFailedAttempts() + 1
        );

        otpRepository.save(deliveryOtp);

        throw new IllegalArgumentException("Invalid OTP.");
    }

    deliveryOtp.setUsed(true);

    otpRepository.save(deliveryOtp);
}
@Transactional(readOnly = true)
public boolean isOtpVerified(Long deliveryId) {

    DeliveryOtp deliveryOtp = otpRepository
            .findByDeliveryId(deliveryId)
            .orElse(null);

    return deliveryOtp != null && deliveryOtp.isUsed();
}
}