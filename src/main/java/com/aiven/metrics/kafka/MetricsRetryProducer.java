/*
 Copyright ...
 */
package com.aiven.metrics.kafka;

import com.aiven.metrics.model.MetricsRetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;


@Service
@Slf4j
public class MetricsRetryProducer {
    public static final String TOPIC = "MetricsRetry";  //this can be defined as configuration.
    private KafkaTemplate<String, MetricsRetry> kafkaTemplate;

    public MetricsRetryProducer(KafkaTemplate<String, MetricsRetry> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public MetricsRetry sendMetrics(MetricsRetry metricsRetry) {
        if (metricsRetry == null || metricsRetry.getMetrics() == null) {
            log.info("message ignored by  MetricsRetryProducer");
            return metricsRetry;
        }
        log.debug("retry metrics for machine:" + metricsRetry.getMetrics().getMachineId());
        ListenableFuture<SendResult<String, MetricsRetry>> future = this.kafkaTemplate.send(TOPIC, metricsRetry);
        try {
            SendResult<String, MetricsRetry> resultMetrics = future.get();
            return resultMetrics.getProducerRecord().value();
        } catch (InterruptedException | ExecutionException e) {
            //retry topic has issue.
            log.error(e.getMessage());

            //TODO: talk with product manager to find what to do here
        }

        return null;
    }
}
