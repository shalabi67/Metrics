package com.aiven.metrics.systems;

import com.aiven.metrics.controller.MetricsController;
import com.aiven.metrics.controller.MetricsService;
import com.aiven.metrics.kafka.MetricsProducer;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

public class MetricsSystem {
    private MetricsController metricsController;
    public static MetricsSystem create(
            KafkaTemplate<String, Metrics> kafkaTemplate,
            KafkaTemplate<String, MetricsRetry> kafkaRetryTemplate) {
        MetricsSystem metricsSystem = new MetricsSystem();

        MetricsService metricsService = metricsSystem.createMetricsService(kafkaTemplate, kafkaRetryTemplate);
        metricsSystem.metricsController = new MetricsController(metricsService);

        return metricsSystem;
    }
    public ResponseEntity<Metrics> addMetrics(Metrics metrics) {
        return metricsController.addMetrics(metrics);
    }

    private MetricsService createMetricsService(
            KafkaTemplate<String, Metrics> kafkaTemplate,
            KafkaTemplate<String, MetricsRetry> kafkaRetryTemplate) {
        MetricsRetryProducer metricsRetryProducer = new MetricsRetryProducer(kafkaRetryTemplate);
        MetricsProducer metricsProducer = new MetricsProducer(kafkaTemplate, metricsRetryProducer);

        return new MetricsService(metricsProducer);
    }
}
