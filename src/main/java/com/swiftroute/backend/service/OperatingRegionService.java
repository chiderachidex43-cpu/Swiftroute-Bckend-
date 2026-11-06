
package com.swiftroute.backend.service;

import com.swiftroute.backend.model.OperatingRegion;
import com.swiftroute.backend.repository.OperatingRegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
public class OperatingRegionService {

    private final OperatingRegionRepository operatingRegionRepository;

    public OperatingRegionService(
            OperatingRegionRepository operatingRegionRepository) {
        this.operatingRegionRepository = operatingRegionRepository;
    }

    public List<OperatingRegion> getActiveRegions() {
        return operatingRegionRepository.findByActiveTrue();
    }

    public OperatingRegion getActiveRegion(String regionId) {
        return operatingRegionRepository.findById(regionId)
                .filter(OperatingRegion::isActive)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Operating region not found or inactive"));
    }

    @Transactional
    public OperatingRegion createRegion(
            String id,
            String countryCode,
            String timeZone) {

        if (id == null || id.isBlank()) {
            throw new RuntimeException("Region ID is required");
        }

        if (countryCode == null
                || !countryCode.matches("^[A-Za-z]{2}$")) {
            throw new RuntimeException(
                    "Country code must contain two letters");
        }

        if (timeZone == null || timeZone.isBlank()) {
            throw new RuntimeException("Timezone is required");
        }

        try {
            ZoneId.of(timeZone);
        } catch (DateTimeException e) {
            throw new RuntimeException("Invalid timezone");
        }

        String normalizedId = id.trim().toUpperCase(Locale.ROOT);
        String normalizedCountryCode =
                countryCode.toUpperCase(Locale.ROOT);

        if (operatingRegionRepository.existsById(normalizedId)) {
            throw new RuntimeException(
                    "Operating region ID already exists");
        }

        OperatingRegion region = new OperatingRegion(
                normalizedId,
                normalizedCountryCode,
                timeZone
        );

        return operatingRegionRepository.save(region);
    }
}
