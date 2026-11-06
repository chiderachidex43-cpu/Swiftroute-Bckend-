
package com.swiftroute.backend.service;

import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryMedia;
import com.swiftroute.backend.model.DeliveryMediaType;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.model.User;
import com.swiftroute.backend.repository.DeliveryMediaRepository;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.UserRepository;
import com.swiftroute.backend.model.DeliveryMediaType;
import com.swiftroute.backend.model.DeliveryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class DeliveryMediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;

    private final DeliveryMediaRepository mediaRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public DeliveryMediaService(
            DeliveryMediaRepository mediaRepository,
            DeliveryRepository deliveryRepository,
            UserRepository userRepository) {

        this.mediaRepository = mediaRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeliveryMedia upload(
            Long deliveryId,
            String userEmail,
            MultipartFile file) throws IOException {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        if (!canAccessDelivery(user, delivery)) {
            throw new RuntimeException(
                    "You are not authorized to access this delivery");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Each file must be 25 MB or smaller");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(
                        contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Upload JPEG, PNG, WEBP, "
                            + "MP4, WEBM, or MOV files");
        }

        DeliveryMedia media = new DeliveryMedia();
        media.setDelivery(delivery);
        media.setMediaType(DeliveryMediaType.PACKAGE);
        media.setFileName(safeFileName(file.getOriginalFilename()));
        media.setContentType(contentType.toLowerCase());
        media.setFileData(file.getBytes());
        media.setFileSize(file.getSize());

        return mediaRepository.save(media);
    }

    @Transactional
public DeliveryMedia uploadDeliveryProof(
        Long deliveryId,
        String riderEmail,
        MultipartFile file) throws IOException {

    User rider = userRepository.findByEmail(riderEmail)
            .orElseThrow(() ->
                    new RuntimeException("Rider not found"));

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    // Only riders can upload delivery proof.
    if (rider.getRole() != Role.RIDER) {
        throw new RuntimeException(
                "Only riders can upload delivery proof");
    }

    // Only the rider assigned to this delivery can upload proof.
    if (delivery.getRider() == null
            || !delivery.getRider().getId().equals(rider.getId())) {
        throw new RuntimeException(
                "You are not the assigned rider for this delivery");
    }

    // Only approved riders can upload delivery proof.
if (!rider.isRiderApproved()) {
    throw new RuntimeException(
            "Rider must be approved to upload delivery proof");
}

    // Proof can only be uploaded after pickup.
    if (delivery.getStatus() != DeliveryStatus.PICKED_UP
            && delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
        throw new IllegalArgumentException(
                "Delivery proof can only be uploaded after pickup");
    }

    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException(
                "Please select a proof photo");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
        throw new IllegalArgumentException(
                "Proof photo must be 25 MB or smaller");
    }

    String contentType = file.getContentType();

    if (contentType == null
            || !Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            ).contains(contentType.toLowerCase())) {
        throw new IllegalArgumentException(
                "Delivery proof must be a JPEG, PNG, or WEBP image");
    }

    DeliveryMedia media = new DeliveryMedia();
    media.setDelivery(delivery);
    media.setMediaType(DeliveryMediaType.DELIVERY_PROOF);
    media.setFileName(
            safeFileName(file.getOriginalFilename()));
    media.setContentType(contentType.toLowerCase());
    media.setFileData(file.getBytes());
    media.setFileSize(file.getSize());

    return mediaRepository.save(media);
}

    @Transactional(readOnly = true)
    public List<DeliveryMedia> getMedia(
            Long deliveryId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        if (!canAccessDelivery(user, delivery)) {
            throw new RuntimeException(
                    "You are not authorized to access this delivery");
        }

        return mediaRepository
                .findByDeliveryOrderByUploadedAtAsc(delivery);
    }

    @Transactional(readOnly = true)
public DeliveryMedia getMediaFile(
        Long deliveryId,
        Long mediaId,
        String userEmail) {

    User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    if (!canAccessDelivery(user, delivery)) {
        throw new RuntimeException(
                "You are not authorized to access this file");
    }

    return mediaRepository.findByIdAndDelivery(mediaId, delivery)
            .orElseThrow(() ->
                    new RuntimeException("Media file not found"));
}

    private boolean canAccessDelivery(
            User user,
            Delivery delivery) {

        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() == Role.CUSTOMER) {
            return delivery.getCustomer() != null
                    && delivery.getCustomer().getId()
                            .equals(user.getId());
        }

        if (user.getRole() == Role.RIDER) {
            return delivery.getRider() != null
                    && delivery.getRider().getId()
                            .equals(user.getId());
        }

        return false;
    }

    private String safeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "package-media";
        }

        String normalized = originalName.replace('\\', '/');
        String name = normalized.substring(
                normalized.lastIndexOf('/') + 1);

        return name.length() > 255
                ? name.substring(name.length() - 255)
                : name;
    }

    @Transactional(readOnly = true)
public boolean hasDeliveryProof(Long deliveryId) {

    Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() ->
                    new RuntimeException("Delivery not found"));

    return mediaRepository.existsByDeliveryAndMediaType(
            delivery,
            DeliveryMediaType.DELIVERY_PROOF
    );
}
}
