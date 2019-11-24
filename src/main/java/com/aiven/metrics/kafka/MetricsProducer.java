/*
 Copyright ...
 */
package com.aiven.metrics.kafka;

import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;


@Service
@Slf4j
public class MetricsProducer {
    public static final String TOPIC = "Metrics";  //this can be defined as configuration.
    private KafkaTemplate<String, Metrics> kafkaTemplate;
    private MetricsRetryProducer metricsRetryProducer;

    public MetricsProducer(
            KafkaTemplate<String, Metrics> kafkaTemplate,
            MetricsRetryProducer metricsRetryProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.metricsRetryProducer = metricsRetryProducer;
    }

    public Metrics sendMetrics(Metrics metrics) {
        log.debug("adding new metrics for machine:" + metrics.getMachineId());

        try {
            ListenableFuture<SendResult<String, Metrics>> future = this.kafkaTemplate.send(TOPIC, metrics);
            SendResult<String, Metrics> resultMetrics = future.get();
            return resultMetrics.getProducerRecord().value();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());

            //let us retry using retry topic.
            MetricsRetry metricsRetry = metricsRetryProducer.sendMetrics(new MetricsRetry(0, metrics));
            return metricsRetry.getMetrics();
        }
    }
}
