
package com.swiftroute.backend.repository;

import com.swiftroute.backend.model.DeliveryMedia;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryMediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryMediaRepository
        extends JpaRepository<DeliveryMedia, Long> {

    List<DeliveryMedia> findByDeliveryOrderByUploadedAtAsc(
            Delivery delivery);

    Optional<DeliveryMedia> findByIdAndDelivery(
            Long id,
            Delivery delivery);

            boolean existsByDeliveryAndMediaType(
        Delivery delivery,
        DeliveryMediaType mediaType
);
}
