package com.aiven.metrics.systems;

import com.aiven.metrics.configuration.ConsumerConfiguration;
import com.aiven.metrics.kafka.AsyncMetrics;
import com.aiven.metrics.kafka.MetricsConsumer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRepository;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MetricsConsumerSystem {
    public static final Long METRICS_ID = 1L;
    private MetricsConsumer metricsConsumer;
    private int retryCount = 3;
    private Metrics lastConsumedMetric;

    public static MetricsConsumerSystem create() {
        MetricsConsumerSystem metricsConsumerSystem = new MetricsConsumerSystem();
        MetricsRepository metricsRepository = metricsConsumerSystem.createMetricsRepository();
        ConsumerConfiguration consumerConfiguration = null; //metricsConsumerSystem.createConsumerConfiguration();
        MetricsRetryProducer metricsRetryProducer = null; //metricsConsumerSystem.createMetricsRetryProducer();
        AsyncMetrics asyncMetrics = new AsyncMetrics(
                metricsRepository,
                consumerConfiguration,
                metricsRetryProducer);

        metricsConsumerSystem.metricsConsumer = new MetricsConsumer(asyncMetrics);
        return metricsConsumerSystem;
    }

    public void consumeMetrics(Metrics metrics) {
        metricsConsumer.consumeMetrics(metrics);
    }
    public Metrics getLastConsumedMetric() {
        return this.lastConsumedMetric;
    }

    private ConsumerConfiguration createConsumerConfiguration() {
        ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        when(consumerConfiguration.getRetryCount()).thenReturn(retryCount);

        return consumerConfiguration;
    }

    private MetricsRetryProducer createMetricsRetryProducer() {
        return new MetricsRetryProducer(KafkaTemplateSystem.createMetricRetryKafkaTemplate());
    }

    private MetricsRepository createMetricsRepository() {
        MetricsRepository metricsRepository = Mockito.mock(MetricsRepository.class);
        when(metricsRepository.save(any())).thenAnswer(new Answer<Metrics>() {
            @Override
            public Metrics answer(InvocationOnMock invocationOnMock) throws Throwable {
                Metrics metrics = invocationOnMock.getArgument(0);
                if(metrics == null) {
                    return null;
                }

                //copy metrics
                Metrics newMetrics = new Metrics();
                newMetrics.setId(METRICS_ID);
                newMetrics.setMachineId(metrics.getMachineId());
                newMetrics.setCpu(metrics.getCpu());
                newMetrics.setMemory(metrics.getMemory());
                newMetrics.setMetricsDate(metrics.getMetricsDate());

                lastConsumedMetric = newMetrics;

                return newMetrics;
            }
        });

        return metricsRepository;
    }
}
