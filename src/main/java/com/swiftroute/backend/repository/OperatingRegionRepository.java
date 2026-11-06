
package com.swiftroute.backend.repository;

import com.swiftroute.backend.model.OperatingRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperatingRegionRepository
        extends JpaRepository<OperatingRegion, String> {

    List<OperatingRegion> findByActiveTrue();
}
