package com.aiven.metrics.kafka;

import com.aiven.metrics.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsRetryConsumer {
    private AsyncMetrics asyncMetrics;

    public MetricsRetryConsumer(AsyncMetrics asyncMetrics) {
        this.asyncMetrics = asyncMetrics;
    }

    @KafkaListener(topics = MetricsRetryProducer.TOPIC)
    public void consumeMetrics(Metrics metrics) {
        asyncMetrics.consume(metrics);
    }


}
