package com.asyncsite.notiservice.adapter.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ChannelStatistics {
    
    private final Period period;
    private final String channelType;
    private final Statistics statistics;
    private final List<DailyChannelStats> dailyStats;
    
    @Getter
    @Builder
    public static class Period {
        private final LocalDate startDate;
        private final LocalDate endDate;
    }
    
    @Getter
    @Builder
    public static class Statistics {
        private final int totalSent;
        private final int successful;
        private final int failed;
        private final double successRate;
        private final String avgDeliveryTime;
        private final double bounceRate;
        private final double openRate;
        private final double clickRate;
    }
    
    @Getter
    @Builder
    public static class DailyChannelStats {
        private final LocalDate date;
        private final int sent;
        private final int successful;
        private final int failed;
    }
} 