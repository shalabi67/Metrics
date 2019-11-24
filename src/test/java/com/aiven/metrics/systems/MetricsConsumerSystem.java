package com.aiven.metrics.systems;

import com.aiven.metrics.configuration.ConsumerConfiguration;
import com.aiven.metrics.kafka.AsyncMetrics;
import com.aiven.metrics.kafka.MetricsConsumer;
import com.aiven.metrics.kafka.MetricsRetryConsumer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.function.Consumer;

import static org.mockito.Mockito.when;

public class MetricsConsumerSystem {
    public static final int RETRY_COUNT = 3;
    private MetricsConsumer metricsConsumer;
    private MetricsRetryConsumer metricsRetryConsumer;
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

    public static MetricsConsumerSystem createWithRetryForConsume() {
        MetricsConsumerSystem metricsConsumerSystem = new MetricsConsumerSystem();
        buildConsumerSystem(
                metricsConsumerSystem,
                MetricsRepositorySystem.createThrowingMetricsRepositorySystem(),
                createConsumerConfiguration(),
                KafkaTemplateSystem.createMetricRetryKafkaTemplate(createConsumer(metricsConsumerSystem)));

        return metricsConsumerSystem;
    }

    private static MetricsConsumerSystem create(
            MetricsRepositorySystem metricsRepositorySystem,
            ConsumerConfiguration consumerConfiguration,
            KafkaTemplate kafkaTemplate) {

        MetricsConsumerSystem metricsConsumerSystem = new MetricsConsumerSystem();
        buildConsumerSystem(metricsConsumerSystem, metricsRepositorySystem, consumerConfiguration, kafkaTemplate);
        return metricsConsumerSystem;
    }
    private static void buildConsumerSystem(
            MetricsConsumerSystem metricsConsumerSystem,
            MetricsRepositorySystem metricsRepositorySystem,
            ConsumerConfiguration consumerConfiguration,
            KafkaTemplate kafkaTemplate) {
        metricsConsumerSystem.metricsRepositorySystem = metricsRepositorySystem;
        metricsConsumerSystem.kafkaTemplate = kafkaTemplate;

        AsyncMetrics asyncMetrics = new AsyncMetrics(
                metricsRepositorySystem.getMetricsRepository(),
                consumerConfiguration,
                new MetricsRetryProducer(metricsConsumerSystem.kafkaTemplate));

        metricsConsumerSystem.metricsConsumer = new MetricsConsumer(asyncMetrics);
        metricsConsumerSystem.metricsRetryConsumer = new MetricsRetryConsumer(asyncMetrics);
    }
    private static Consumer<MetricsRetry> createConsumer(MetricsConsumerSystem metricsConsumerSystem) {
        Consumer<MetricsRetry> consumer = (MetricsRetry metricsRetry) -> {
            metricsConsumerSystem.consumeMetrics(metricsRetry);
        };

        return consumer;
    }
    private static ConsumerConfiguration createConsumerConfiguration() {
        ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        when(consumerConfiguration.getRetryCount()).thenReturn(RETRY_COUNT);

        return consumerConfiguration;
    }

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
