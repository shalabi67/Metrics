package com.aiven.metrics.metrics;

import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.systems.MetricsConsumerSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetricsConsumingTest {
    private Long machineId = 4L;

    @Test
    public void testConsumeMetric() {
        MetricsConsumerSystem metricsConsumerSystem = MetricsConsumerSystem.create();
        Metrics metrics = createMetrics(machineId);

        metricsConsumerSystem.consumeMetrics(metrics);
        Metrics consumedMetrics = metricsConsumerSystem.getLastConsumedMetric();

        Assertions.assertNotNull(consumedMetrics);
        Assertions.assertEquals(MetricsConsumerSystem.METRICS_ID, consumedMetrics.getId());
        Assertions.assertEquals(metrics.getMachineId(), consumedMetrics.getMachineId());
    }

    private Metrics createMetrics(Long machineId) {
        Metrics metrics = new Metrics();
        metrics.setMachineId(machineId);
        metrics.setMemory(20L);
        metrics.setId(null);

        return metrics;
    }
}
