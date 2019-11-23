/*
 Copyright ...
 */
package com.aiven.metrics.controller;

import com.aiven.metrics.kafka.MetricsProducer;
import com.aiven.metrics.model.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsService {
    private MetricsProducer metricsProducer;

    public MetricsService(MetricsProducer metricsProducer) {
        this.metricsProducer = metricsProducer;
    }

    public ResponseEntity<Metrics> addMetrics(Metrics metrics) {
        if (metrics == null || metrics.getMachineId() == null) {
            log.debug("metrics or machine id is null.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        metrics.setId(null);
        Metrics resultMetrics = metricsProducer.sendMetrics(metrics);
        if(resultMetrics == null) {
            return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }

        return new ResponseEntity<>(metrics, HttpStatus.CREATED);
    }
}
