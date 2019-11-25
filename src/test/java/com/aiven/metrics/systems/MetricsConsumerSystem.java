package com.aiven.metrics.systems;

import com.aiven.metrics.kafka.MetricsConsumer;
import com.aiven.metrics.kafka.MetricsRetryConsumer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import lombok.Setter;
import org.springframework.kafka.core.KafkaTemplate;

@Setter
public class MetricsConsumerSystem {
    public static final int RETRY_COUNT = 3;
    private MetricsConsumer metricsConsumer;
    private MetricsRetryConsumer metricsRetryConsumer;
    private MetricsRepositorySystem metricsRepositorySystem;
    private KafkaTemplate kafkaTemplate;

    public void consumeMetrics(Metrics metrics) {
        metricsConsumer.consumeMetrics(metrics);
    }

    public void consumeMetrics(MetricsRetry metrics) {
        metricsRetryConsumer.consumeMetrics(metrics);
    }

    public Metrics getLastConsumedMetric() {
        return metricsRepositorySystem.getLastConsumedMetric();
    }

    public KafkaTemplate getKafkaTemplate() {
        return this.kafkaTemplate;
    }
}
