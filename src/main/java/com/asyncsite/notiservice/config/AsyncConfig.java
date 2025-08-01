package com.asyncsite.notiservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${application.notification.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${application.notification.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${application.notification.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${application.notification.async.thread-name-prefix:noti-async-}")
    private String threadNamePrefix;

    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
} 