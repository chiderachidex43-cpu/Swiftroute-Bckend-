package com.swiftroute.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminDashboardResponse(

        long totalDeliveries,
        long activeDeliveries,
        long pendingDeliveries,
        long deliveredDeliveries,
        long deliveriesCreatedToday,

        long totalCustomers,
        long approvedActiveRiders,

        List<DailyDeliveryCount> deliveryTrend,
        List<StatusDistribution> statusDistribution,

        List<DeliveryResponse> recentDeliveries

) {

    public record DailyDeliveryCount(
            LocalDate date,
            long count
    ) {}

    public record StatusDistribution(
            String status,
            long count
    ) {}

}