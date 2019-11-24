/*
 Copyright ...
 */
package com.aiven.metrics.kafka;

import com.aiven.metrics.configuration.ConsumerConfiguration;
import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRepository;
import com.aiven.metrics.model.MetricsRetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Process kafka messages async.
 * <p>
 * This class is responsible for writing a Metrics to postgres.
 * If the operation failed it will send that Metrics to a retry kafka topic.
 * </p>
 *
 * @see Metrics
 * @see MetricsRetry
 * @see MetricsRepository
 * @see MetricsRetryProducer
 * @see ConsumerConfiguration
 */
@Service
@Slf4j
public class AsyncMetrics {
    private MetricsRepository metricsRepository;
    private ConsumerConfiguration consumerConfiguration;
    private MetricsRetryProducer metricsRetryProducer;

    /**
     * create a AsyncMetrics object. there wil be only single instance for this class.
     * @param metricsRepository a postgres repository to save Metric
     * @param consumerConfiguration a set of consumer configurations
     * @param metricsRetryProducer
     */
    public AsyncMetrics(
            MetricsRepository metricsRepository,
            ConsumerConfiguration consumerConfiguration,
            MetricsRetryProducer metricsRetryProducer) {
        this.metricsRepository = metricsRepository;
        this.consumerConfiguration = consumerConfiguration;
        this.metricsRetryProducer = metricsRetryProducer;
    }

    /**
     * This is an async method. it will try to save Metrics into database.
     * If the save operation failed, it will send the Metrics to kafka topic using MetricsRetryProducer.
     *
     * @param metrics this is the Metrics to save
     * @return CompletableFuture<Metrics> this value is not used at this stage.
     */
    @Async
    public CompletableFuture<Metrics> consume(Metrics metrics) {
        log.info("Consuming message using thread " + Thread.currentThread().getName());
        if(metrics == null || metrics.getMachineId() == null) {
            log.error("Invalid data.");
            return CompletableFuture.completedFuture(metrics);
        }
        log.info("received metrics of machine id: " + metrics.getMachineId());

        Metrics savedMetrics = metrics;
        try {
            //save to database
            savedMetrics = metricsRepository.save(metrics);
        } catch(Exception e) {
            log.error(e.getMessage());
            //retry the operation
            metricsRetryProducer.sendMetrics(new MetricsRetry(0, metrics));
        }

        return CompletableFuture.completedFuture(savedMetrics);
    }

    /**
     * This is an async method. it will try to save Metrics into database.
     * If the save operation failed, based into the retry count of the MetricsRetry,
     * it will send the Metrics to kafka topic using MetricsRetryProducer
     * @param metricsRetry this
     * @return
     */
    @Async
    public CompletableFuture<MetricsRetry> consume(MetricsRetry metricsRetry) {
        log.info("Consuming message using thread " + Thread.currentThread().getName());
        if(metricsRetry == null || metricsRetry.getMetrics() == null) {
            log.error("Invalid data.");
            return CompletableFuture.completedFuture(metricsRetry);
        }
        log.info("received metrics of machine id: " + metricsRetry.getMetrics().getMachineId());
        Metrics savedMetrics = metricsRetry.getMetrics();
        MetricsRetry newMetricsRetry = metricsRetry;

        try {
            //save to database
            savedMetrics = metricsRepository.save(savedMetrics);
            newMetricsRetry = new MetricsRetry(metricsRetry.getRetryCount(), savedMetrics);
        } catch(Exception e) {
            log.error(e.getMessage());

            if(metricsRetry.getRetryCount() < consumerConfiguration.getRetryCount()) {
                //increment retry count and retry the operation.
                newMetricsRetry = new MetricsRetry(
            metricsRetry.getRetryCount() + 1,
                      metricsRetry.getMetrics());
                metricsRetryProducer.sendMetrics(newMetricsRetry);
            } else {
                //TODO: what we should do. is it ok to discard the message?
            }
        }
        return CompletableFuture.completedFuture(newMetricsRetry);
    }
}
