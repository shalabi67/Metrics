/*
 Copyright ...
 */
package com.aiven.metrics.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;


@Entity
@Data
public class Metrics {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private Long machineId;
    private Date metricsDate;
    private Long cpu;
    private Long memory;
}
