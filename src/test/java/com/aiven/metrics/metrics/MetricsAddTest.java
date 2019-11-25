package com.aiven.metrics.metrics;

import com.aiven.metrics.factory.KafkaTemplateSystemFactory;
import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import com.aiven.metrics.systems.KafkaTemplateSystem;
import com.aiven.metrics.systems.MetricsSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*
test adding metrics through a rest point. the test will cover the rest point call and the submit to kafka topic
test will not cover consuming the topic.
*/
@ExtendWith(MockitoExtension.class)
public class MetricsAddTest {

    @Test
    public void testAddMetrics() {
        //initialize test
        KafkaTemplate<String, Metrics> kafkaTemplate = KafkaTemplateSystemFactory.createMetricKafkaTemplate();
        MetricsSystem metricsSystem = MetricsSystem.create(kafkaTemplate, null);

        //run test
        Long machineId = 10L;
        ResponseEntity<Metrics> response = metricsSystem.addMetrics(createMetrics(machineId));

        //validate
        Metrics metrics = response.getBody();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(machineId, metrics.getMachineId());
    }

    @Test
    public void testAddInvalidMetrics() {
        //initialize test
        MetricsSystem metricsSystem = MetricsSystem.create(null, null);

        //run test
        Long machineId = null;
        ResponseEntity<Metrics> response = metricsSystem.addMetrics(createMetrics(machineId));

        //validate
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testTopicException() {
        KafkaTemplate<String, Metrics> kafkaTemplate = KafkaTemplateSystemFactory.createThrowingMetricKafkaTemplate();
        KafkaTemplate<String, MetricsRetry> kafkaRetryTemplate = KafkaTemplateSystemFactory.createMetricRetryKafkaTemplate();
        MetricsSystem metricsSystem = MetricsSystem.create(kafkaTemplate, kafkaRetryTemplate);

        Long machineId = 10L;
        Metrics metrics = createMetrics(machineId);
        ResponseEntity<Metrics> response = metricsSystem.addMetrics(metrics);
        Metrics resultMetrics = response.getBody();

        verify(kafkaRetryTemplate, times(1)).send(MetricsRetryProducer.TOPIC, new MetricsRetry(0, metrics));
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(machineId, resultMetrics.getMachineId());
    }

    private Metrics createMetrics(Long machineId) {
        Metrics metrics = new Metrics();
        metrics.setMachineId(machineId);

        return metrics;
    }
}
