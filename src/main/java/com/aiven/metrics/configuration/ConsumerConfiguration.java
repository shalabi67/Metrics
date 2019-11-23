/*
 Copyright ...
 */
package com.aiven.metrics.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ConsumerConfiguration {
    @Value("${consumer.start.threads}")
    private int startingThreads;

    @Value("${consumer.maximum.threads}")
    private int maximumThreads;

    @Value("${consumer.maximum.queue}")
    private int queueSize;

    @Value("${consumer.retry}")
    private int retryCount;

    public int getRetryCount() {
        return retryCount;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(startingThreads);
        executor.setMaxPoolSize(maximumThreads);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("MetricsConsumer-");
        executor.initialize();

        return executor;
    }
}
