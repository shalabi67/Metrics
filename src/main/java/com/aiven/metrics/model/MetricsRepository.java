/*
 Copyright ...
 */
package com.aiven.metrics.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricsRepository extends JpaRepository<Metrics, Long> {
}
