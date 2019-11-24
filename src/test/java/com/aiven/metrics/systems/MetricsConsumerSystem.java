package com.aiven.metrics.systems;

import com.aiven.metrics.configuration.ConsumerConfiguration;
import com.aiven.metrics.kafka.AsyncMetrics;
import com.aiven.metrics.kafka.MetricsConsumer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.when;

public class MetricsConsumerSystem {
    private static final int RETRY_COUNT = 3;
    private MetricsConsumer metricsConsumer;
    private MetricsRepositorySystem metricsRepositorySystem;
    private KafkaTemplate kafkaTemplate;

    public static MetricsConsumerSystem create() {
        return create(MetricsRepositorySystem.createMetricsRepositorySystem(), null, null);
    }

    public static MetricsConsumerSystem createWithRetry() {
        return create(
                MetricsRepositorySystem.createThrowingMetricsRepositorySystem(),
                null,
                KafkaTemplateSystem.createMetricRetryKafkaTemplate());
    }

    private static MetricsConsumerSystem create(
            MetricsRepositorySystem metricsRepositorySystem,
            ConsumerConfiguration consumerConfiguration,
            KafkaTemplate kafkaTemplate) {

        MetricsConsumerSystem metricsConsumerSystem = new MetricsConsumerSystem();
        metricsConsumerSystem.metricsRepositorySystem = metricsRepositorySystem;
        metricsConsumerSystem.kafkaTemplate = kafkaTemplate;

        AsyncMetrics asyncMetrics = new AsyncMetrics(
                metricsRepositorySystem.getMetricsRepository(),
                consumerConfiguration,
                new MetricsRetryProducer(metricsConsumerSystem.kafkaTemplate));

        metricsConsumerSystem.metricsConsumer = new MetricsConsumer(asyncMetrics);
        return metricsConsumerSystem;
    }

    private static ConsumerConfiguration createConsumerConfiguration() {
        ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        when(consumerConfiguration.getRetryCount()).thenReturn(RETRY_COUNT);

        return consumerConfiguration;
    }

    public void consumeMetrics(Metrics metrics) {
        metricsConsumer.consumeMetrics(metrics);
    }

    public Metrics getLastConsumedMetric() {
        return metricsRepositorySystem.getLastConsumedMetric();
    }

    public KafkaTemplate getKafkaTemplate() {
        return this.kafkaTemplate;
    }
}
