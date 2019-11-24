package com.aiven.metrics.configuration;

import com.aiven.metrics.kafka.MetricsProducer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfiguration {
    @Value("${metrics.partitions}")
    private int metricsPartitions;

    @Value("${metrics.replication}")
    private short metricsReplicationFactor;

    @Value("${retry.partitions}")
    private int retryPartitions;

    @Value("${retry.replication}")
    private short retryReplicationFactor;

    @Bean
    public NewTopic metricsTopic() {
        return new NewTopic(MetricsProducer.TOPIC, metricsPartitions, metricsReplicationFactor);
    }

    @Bean
    public NewTopic retryTopic() {
        return new NewTopic(MetricsRetryProducer.TOPIC, retryPartitions, retryReplicationFactor);
    }
}
