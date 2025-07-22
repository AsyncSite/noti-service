package com.asyncsite.notiservice.adapter.in.dto;

import java.time.LocalDate;
import java.util.List;

public record ChannelStatistics(
    Period period,
    String channelType,
    Statistics statistics,
    List<DailyChannelStats> dailyStats
) {
    public record Period(
        LocalDate startDate,
        LocalDate endDate
    ) {}
    
    public record Statistics(
        int totalSent,
        int successful,
        int failed,
        double successRate,
        String avgDeliveryTime,
        double bounceRate,
        double openRate,
        double clickRate
    ) {}
    
    public record DailyChannelStats(
        LocalDate date,
        int sent,
        int successful,
        int failed
    ) {}
} 