
package com.swiftroute.backend.controller;

import com.swiftroute.backend.model.DeliveryMedia;
import com.swiftroute.backend.service.DeliveryMediaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries/{deliveryId}/media")
public class DeliveryMediaController {

    private final DeliveryMediaService mediaService;

    public DeliveryMediaController(
            DeliveryMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long deliveryId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        try {
            DeliveryMedia media = mediaService.upload(
                    deliveryId,
                    principal.getName(),
                    file);

            return ResponseEntity.ok(Map.of(
                    "id", media.getId(),
                    "fileName", media.getFileName(),
                    "contentType", media.getContentType(),
                    "fileSize", media.getFileSize(),
                    "uploadedAt", media.getUploadedAt(),
                    "url", "/api/deliveries/"
                            + deliveryId + "/media/"
                            + media.getId()
            ));
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            e.getMessage() == null
                                    ? "Unable to upload file"
                                    : e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listMedia(
            @PathVariable Long deliveryId,
            Principal principal) {

        try {
            List<Map<String, Object>> result =
                    mediaService.getMedia(
                                    deliveryId,
                                    principal.getName())
                            .stream()
                            .map(media -> Map.<String, Object>of(
                                    "id", media.getId(),
                                    "fileName", media.getFileName(),
                                    "contentType", media.getContentType(),
                                    "fileSize", media.getFileSize(),
                                    "uploadedAt", media.getUploadedAt(),
                                    "url", "/api/deliveries/"
                                            + deliveryId + "/media/"
                                            + media.getId()
                            ))
                            .toList();

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{mediaId}")
public ResponseEntity<?> getMediaFile(
        @PathVariable Long deliveryId,
        @PathVariable Long mediaId,
        Principal principal) {

    try {
        DeliveryMedia media = mediaService.getMediaFile(
                deliveryId,
                mediaId,
                principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        media.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + media.getFileName().replace("\"", "")
                                + "\"")
                .body(media.getFileData());

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(
                Map.of("message",
                        e.getMessage() == null
                                ? "Unable to retrieve file"
                                : e.getMessage()));
    }
}
}
