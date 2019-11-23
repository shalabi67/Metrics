/*
 Copyright ...
 */
package com.aiven.metrics.controller;

import com.aiven.metrics.model.Metrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class MetricsController {
    private MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<Metrics> addMetrics(@RequestBody Metrics metrics) {
        return metricsService.addMetrics(metrics);
    }
}
