package com.asyncsite.notiservice.config;

import com.asyncsite.notiservice.domain.port.out.NotificationQueuePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Test configuration to provide required beans for testing
 */
@TestConfiguration
public class TestQueueConfiguration {

    @Bean
    @Primary
    public ThreadPoolTaskScheduler testTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("test-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}