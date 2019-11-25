package com.aiven.metrics.factory;

import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import com.aiven.metrics.systems.KafkaTemplateSystem;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.function.Consumer;

public class KafkaTemplateSystemFactory {
    public static KafkaTemplate<String, Metrics> createMetricKafkaTemplate() {
        KafkaTemplateSystem<String, Metrics> kafkaTemplateSystem = new KafkaTemplateSystem<>();
        return kafkaTemplateSystem.createKafkaTemplate(kafkaTemplateSystem.getDefaultKafkaSend());
    }

    public static KafkaTemplate<String, Metrics> createThrowingMetricKafkaTemplate() {
        KafkaTemplateSystem<String, Metrics> kafkaTemplateSystem = new KafkaTemplateSystem<>();
        return kafkaTemplateSystem.createKafkaTemplate(invocationOnMock -> {throw new InterruptedException();});
    }

    public static KafkaTemplate<String, MetricsRetry> createMetricRetryKafkaTemplate() {
        KafkaTemplateSystem<String, MetricsRetry> kafkaTemplateSystem = new KafkaTemplateSystem<>();
        return kafkaTemplateSystem.createKafkaTemplate(kafkaTemplateSystem.getDefaultKafkaSend());
    }

    public static KafkaTemplate<String, MetricsRetry> createMetricRetryKafkaTemplate(Consumer<MetricsRetry> consumer) {
        KafkaTemplateSystem<String, MetricsRetry> kafkaTemplateSystem = new KafkaTemplateSystem<>();
        kafkaTemplateSystem.setConsumer(consumer);
        return kafkaTemplateSystem.createKafkaTemplate(kafkaTemplateSystem.getDefaultKafkaSend());
    }
}
