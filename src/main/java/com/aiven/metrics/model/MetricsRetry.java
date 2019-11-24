/*
 Copyright ...
 */
package com.aiven.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricsRetry {
    private int retryCount;
    private Metrics metrics;
    public MetricsRetry() {

    }
}
