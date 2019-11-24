package com.aiven.metrics.metrics;

import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import com.aiven.metrics.systems.MetricsConsumerSystem;
import com.aiven.metrics.systems.MetricsRepositorySystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricsConsumingTest {
    private Long machineId = 100L;

    @Test
    void testConsumeMetric() {
        MetricsConsumerSystem metricsConsumerSystem = MetricsConsumerSystem.create();
        Metrics metrics = createMetrics(machineId);

        metricsConsumerSystem.consumeMetrics(metrics);
        Metrics consumedMetrics = metricsConsumerSystem.getLastConsumedMetric();

        Assertions.assertNotNull(consumedMetrics);
        Assertions.assertEquals(MetricsRepositorySystem.METRICS_ID, consumedMetrics.getId());
        Assertions.assertEquals(metrics.getMachineId(), consumedMetrics.getMachineId());
    }

    @Test
    void testHavingDatabaseException() {

        MetricsConsumerSystem metricsConsumerSystem = MetricsConsumerSystem.createWithRetryForConsume();
        Metrics metrics = createMetrics(machineId);

        metricsConsumerSystem.consumeMetrics(metrics);
        Metrics consumedMetrics = metricsConsumerSystem.getLastConsumedMetric();

        Assertions.assertNull(consumedMetrics);
        KafkaTemplate kafkaTemplate = metricsConsumerSystem.getKafkaTemplate();
        verifyCallCount(metricsConsumerSystem.getKafkaTemplate(), metrics, 0, 1);
    }

    @Test
    void testRetry() {
        MetricsConsumerSystem metricsConsumerSystem = MetricsConsumerSystem.createWithRetryForConsume();
        Metrics metrics = createMetrics(machineId);

        metricsConsumerSystem.consumeMetrics(metrics);
        Metrics consumedMetrics = metricsConsumerSystem.getLastConsumedMetric();

        Assertions.assertNull(consumedMetrics);
        KafkaTemplate kafkaTemplate = metricsConsumerSystem.getKafkaTemplate();

        for(int i=0;i<=MetricsConsumerSystem.RETRY_COUNT; i++) {
            verifyCallCount(kafkaTemplate, metrics, i, 1);
        }
        verifyCallCount(kafkaTemplate, metrics, MetricsConsumerSystem.RETRY_COUNT + 1, 0);
    }

    private void verifyCallCount(KafkaTemplate kafkaTemplate, Metrics metrics, int retryCount, int expectedCalls) {
        verify(kafkaTemplate,
                times(expectedCalls)).send(MetricsRetryProducer.TOPIC, new MetricsRetry(retryCount, metrics));
    }
    private Metrics createMetrics(Long machineId) {
        Metrics metrics = new Metrics();
        metrics.setMachineId(machineId);
        metrics.setMemory(20L);
        metrics.setId(null);

        return metrics;
    }
}
