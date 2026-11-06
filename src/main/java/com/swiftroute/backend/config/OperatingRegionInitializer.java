
package com.swiftroute.backend.config;

import com.swiftroute.backend.model.OperatingRegion;
import com.swiftroute.backend.repository.OperatingRegionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperatingRegionInitializer {

    @Bean
    CommandLineRunner initializeOperatingRegions(
            OperatingRegionRepository operatingRegionRepository) {

        return args -> {
            if (!operatingRegionRepository.existsById("NG")) {
                operatingRegionRepository.save(
                    new OperatingRegion(
                        "NG",
                        "NG",
                        "Africa/Lagos"
                    )
                );
            }
        };
    }
}
