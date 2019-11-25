package com.aiven.metrics.factory;

import com.aiven.metrics.configuration.ConsumerConfiguration;
import com.aiven.metrics.kafka.AsyncMetrics;
import com.aiven.metrics.kafka.MetricsConsumer;
import com.aiven.metrics.kafka.MetricsRetryConsumer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.MetricsRetry;
import com.aiven.metrics.systems.MetricsConsumerSystem;
import com.aiven.metrics.systems.MetricsRepositorySystem;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.function.Consumer;

import static org.mockito.Mockito.when;

public class MetricsConsumerSystemFactory {
    public static MetricsConsumerSystem create() {
        return create(MetricsRepositorySystem.createMetricsRepositorySystem(), null, null);
    }

    public static MetricsConsumerSystem createWithRetryForConsume() {
        MetricsConsumerSystem metricsConsumerSystem = new MetricsConsumerSystem();
        buildConsumerSystem(
                metricsConsumerSystem,
                MetricsRepositorySystem.createThrowingMetricsRepositorySystem(),
                createConsumerConfiguration(),
                KafkaTemplateSystemFactory.createMetricRetryKafkaTemplate(createConsumer(metricsConsumerSystem)));

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
        metricsConsumerSystem.setMetricsRepositorySystem(metricsRepositorySystem);
        metricsConsumerSystem.setKafkaTemplate(kafkaTemplate);

        AsyncMetrics asyncMetrics = new AsyncMetrics(
                metricsRepositorySystem.getMetricsRepository(),
                consumerConfiguration,
                new MetricsRetryProducer(metricsConsumerSystem.getKafkaTemplate()));

        metricsConsumerSystem.setMetricsConsumer(new MetricsConsumer(asyncMetrics));
        metricsConsumerSystem.setMetricsRetryConsumer(new MetricsRetryConsumer(asyncMetrics));
    }
    private static Consumer<MetricsRetry> createConsumer(MetricsConsumerSystem metricsConsumerSystem) {
        Consumer<MetricsRetry> consumer = (MetricsRetry metricsRetry) -> {
            metricsConsumerSystem.consumeMetrics(metricsRetry);
        };

        return consumer;
    }
    private static ConsumerConfiguration createConsumerConfiguration() {
        ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        when(consumerConfiguration.getRetryCount()).thenReturn(MetricsConsumerSystem.RETRY_COUNT);

        return consumerConfiguration;
    }
}
