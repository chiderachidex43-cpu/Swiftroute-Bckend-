
package com.swiftroute.backend.controller;

import com.swiftroute.backend.model.OperatingRegion;
import com.swiftroute.backend.service.OperatingRegionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OperatingRegionController {

    private final OperatingRegionService operatingRegionService;

    public OperatingRegionController(
            OperatingRegionService operatingRegionService) {
        this.operatingRegionService = operatingRegionService;
    }

    @GetMapping("/operating-regions")
    public List<OperatingRegion> getActiveRegions() {
        return operatingRegionService.getActiveRegions();
    }

    @PostMapping("/admin/operating-regions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public OperatingRegion createRegion(
            @RequestBody Map<String, String> request) {

        return operatingRegionService.createRegion(
                request.get("id"),
                request.get("countryCode"),
                request.get("timeZone")
        );
    }
}
