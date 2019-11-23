/*
 Copyright ...
 */
package com.aiven.metrics.kafka;

import com.aiven.metrics.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumes Kafak message by calling async consume operation.
 *
 * @see AsyncMetrics
 * @see Metrics
 */
@Service
@Slf4j
public class MetricsConsumer {
    private AsyncMetrics asyncMetrics;

    public MetricsConsumer(AsyncMetrics asyncMetrics) {
        this.asyncMetrics = asyncMetrics;
    }

    @KafkaListener(topics = MetricsProducer.TOPIC)
    public void consumeMetrics(Metrics metrics) {
        asyncMetrics.consume(metrics);
    }


}
