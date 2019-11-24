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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricsConsumingTest {
    private Long machineId = 4L;

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
        MetricsConsumerSystem metricsConsumerSystem = MetricsConsumerSystem.createWithRetry();
        Metrics metrics = createMetrics(machineId);

        metricsConsumerSystem.consumeMetrics(metrics);
        Metrics consumedMetrics = metricsConsumerSystem.getLastConsumedMetric();

        Assertions.assertNull(consumedMetrics);
        verify(metricsConsumerSystem.getKafkaTemplate(),
                times(1)).send(MetricsRetryProducer.TOPIC, new MetricsRetry(0, metrics));
    }

    private Metrics createMetrics(Long machineId) {
        Metrics metrics = new Metrics();
        metrics.setMachineId(machineId);
        metrics.setMemory(20L);
        metrics.setId(null);

        return metrics;
    }
}
