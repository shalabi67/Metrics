package com.aiven.metrics.metrics;


import com.aiven.metrics.kafka.MetricsRetryProducer;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import com.aiven.metrics.systems.KafkaTemplateSystem;
import com.aiven.metrics.systems.MetricsSystem;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class MetricsAddTest {
    /*
    test adding metrics through a rest point. the test will cover the rest point call and the submit to kafka topic
    test will not cover consuming the topic.
     */
    @Test
    public void testAddMetrics() {
        //initialize test
        KafkaTemplate<String, Metrics> kafkaTemplate = KafkaTemplateSystem.createMetricKafkaTemplate();
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
        KafkaTemplate<String, Metrics> kafkaTemplate = createKafkaThrowTemplate();
        KafkaTemplate<String, MetricsRetry> kafkaRetryTemplate = createKafkaRetryTemplate();
        MetricsSystem metricsSystem = MetricsSystem.create(kafkaTemplate, kafkaRetryTemplate);

        Long machineId = 10L;
        Metrics metrics = createMetrics(machineId);
        ResponseEntity<Metrics> response = metricsSystem.addMetrics(metrics);
        Metrics resultMetrics = response.getBody();
        verify(kafkaRetryTemplate, times(1)).send(MetricsRetryProducer.TOPIC, new MetricsRetry(0, metrics));
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(machineId, metrics.getMachineId());
    }

    private Metrics createMetrics(Long machineId) {
        Metrics metrics = new Metrics();
        metrics.setMachineId(machineId);

        return metrics;
    }

    private KafkaTemplate<String, Metrics> createKafkaTemplate() {
        KafkaTemplate<String, Metrics> kafkaTemplate = (KafkaTemplate<String, Metrics>)Mockito.mock(KafkaTemplate.class);
        if(kafkaTemplate == null) {
            Assertions.fail();
        }

        Mockito.when(kafkaTemplate.send(anyString(), any(Metrics.class))).thenAnswer(
                new Answer<ListenableFuture<SendResult<String, Metrics>>>() {
            @Override
            public ListenableFuture<SendResult<String, Metrics>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Metrics metrics = invocationOnMock.getArgument(1);
                SettableListenableFuture<SendResult<String, Metrics>> future = new SettableListenableFuture<>();
                future.set(new SendResult<String, Metrics>(new ProducerRecord<>("", metrics), null));
                return future;
            }
        });

        return kafkaTemplate;
    }
    private KafkaTemplate<String, Metrics> createKafkaThrowTemplate() {
        KafkaTemplate<String, Metrics> kafkaTemplate = (KafkaTemplate<String, Metrics>)Mockito.mock(KafkaTemplate.class);
        if(kafkaTemplate == null) {
            Assertions.fail();
        }

        Mockito.when(kafkaTemplate.send(anyString(), any(Metrics.class))).thenAnswer(
                new Answer<ListenableFuture<SendResult<String, Metrics>>>() {
                    @Override
                    public ListenableFuture<SendResult<String, Metrics>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        throw new InterruptedException();
                    }
                });

        return kafkaTemplate;
    }
    private KafkaTemplate<String, MetricsRetry> createKafkaRetryTemplate() {
        KafkaTemplate<String, MetricsRetry> kafkaTemplate = (KafkaTemplate<String, MetricsRetry>)Mockito.mock(KafkaTemplate.class);
        if(kafkaTemplate == null) {
            Assertions.fail();
        }

        Mockito.when(kafkaTemplate.send(anyString(), any(MetricsRetry.class))).thenAnswer(
                new Answer<ListenableFuture<SendResult<String, MetricsRetry>>>() {
                    @Override
                    public ListenableFuture<SendResult<String, MetricsRetry>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        MetricsRetry metrics = invocationOnMock.getArgument(1);
                        SettableListenableFuture<SendResult<String, MetricsRetry>> future = new SettableListenableFuture<>();
                        future.set(new SendResult<String, MetricsRetry>(new ProducerRecord<>(MetricsRetryProducer.TOPIC, metrics), null));
                        return future;
                    }
                });

        return kafkaTemplate;
    }
}
