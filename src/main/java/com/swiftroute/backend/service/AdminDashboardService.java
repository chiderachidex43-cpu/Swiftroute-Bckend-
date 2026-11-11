
package com.swiftroute.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swiftroute.backend.dto.AdminDashboardResponse;
import com.swiftroute.backend.dto.DeliveryResponse;
import com.swiftroute.backend.model.AccountStatus;
import com.swiftroute.backend.model.Delivery;
import com.swiftroute.backend.model.DeliveryStatus;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.repository.DeliveryRepository;
import com.swiftroute.backend.repository.UserRepository;

@Service
public class AdminDashboardService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(
            DeliveryRepository deliveryRepository,
            UserRepository userRepository) {

        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        // Overall delivery statistics from PostgreSQL.
        long totalDeliveries = deliveryRepository.count();

        long pendingDeliveries =
                deliveryRepository.countByStatus(
                        DeliveryStatus.REQUESTED);

        long deliveredDeliveries =
                deliveryRepository.countByStatus(
                        DeliveryStatus.DELIVERED);

        long activeDeliveries =
                deliveryRepository.countByStatus(
                        DeliveryStatus.ACCEPTED)
                + deliveryRepository.countByStatus(
                        DeliveryStatus.PICKED_UP)
                + deliveryRepository.countByStatus(
                        DeliveryStatus.IN_TRANSIT);

        // Customer and approved, active rider counts.
        long totalCustomers =
                userRepository.countByRole(Role.CUSTOMER);

        long approvedActiveRiders =
                userRepository
                        .countByRoleAndRiderApprovedTrueAndAccountStatus(
                                Role.RIDER,
                                AccountStatus.ACTIVE);

        /*
         * Delivery.createdAt currently uses LocalDateTime.now().
         * Use the JVM's default date consistently with that timestamp.
         */
        LocalDate today = LocalDate.now();

        long deliveriesCreatedToday =
                countDeliveriesOn(today);

        // Build a seven-day delivery creation trend.
        List<AdminDashboardResponse.DailyDeliveryCount> deliveryTrend =
                new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate date = today.minusDays(i);

            long count = countDeliveriesOn(date);

            deliveryTrend.add(
                    new AdminDashboardResponse.DailyDeliveryCount(
                            date,
                            count));
        }

        // Include every delivery status, including zero counts.
        List<AdminDashboardResponse.StatusDistribution>
                statusDistribution = new ArrayList<>();

        for (DeliveryStatus status : DeliveryStatus.values()) {

            long count =
                    deliveryRepository.countByStatus(status);

            statusDistribution.add(
                    new AdminDashboardResponse.StatusDistribution(
                            status.name(),
                            count));
        }

        // Retrieve only the six most recent deliveries.
        List<DeliveryResponse> recentDeliveries =
                deliveryRepository
                        .findTop6ByOrderByCreatedAtDesc()
                        .stream()
                        .map(DeliveryResponse::from)
                        .toList();

        return new AdminDashboardResponse(
                totalDeliveries,
                activeDeliveries,
                pendingDeliveries,
                deliveredDeliveries,
                deliveriesCreatedToday,
                totalCustomers,
                approvedActiveRiders,
                deliveryTrend,
                statusDistribution,
                recentDeliveries);
    }

    private long countDeliveriesOn(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return deliveryRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        start,
                        end);
    }
}
