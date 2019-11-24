package com.aiven.metrics.systems;

import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRepository;
import lombok.Getter;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@Getter
public class MetricsRepositorySystem {
    public static final Long METRICS_ID = 1L;
    private static final String EXCEPTION_MESSAGE = "Testing repository throwing exception";
    private Metrics lastConsumedMetric;
    private MetricsRepository metricsRepository;

    public static MetricsRepositorySystem createMetricsRepositorySystem() {
        MetricsRepositorySystem metricsRepositorySystem = new MetricsRepositorySystem();
        metricsRepositorySystem.metricsRepository =
                metricsRepositorySystem.createMetricsRepository(metricsRepositorySystem.normalSaving);

        return metricsRepositorySystem;
    }
    public static MetricsRepositorySystem createThrowingMetricsRepositorySystem() {
        MetricsRepositorySystem metricsRepositorySystem = new MetricsRepositorySystem();
        metricsRepositorySystem.metricsRepository =
                metricsRepositorySystem.createMetricsRepository(invocationOnMock -> {throw new Exception(EXCEPTION_MESSAGE);});

        return metricsRepositorySystem;
    }

    private MetricsRepository createMetricsRepository(ThrowableFunction<InvocationOnMock, Metrics> lambda) {
        MetricsRepository metricsRepository = Mockito.mock(MetricsRepository.class);
        when(metricsRepository.save(any())).thenAnswer(new Answer<Metrics>() {
            @Override
            public Metrics answer(InvocationOnMock invocationOnMock) throws Throwable {
                return lambda.apply(invocationOnMock);
            }
        });

        return metricsRepository;
    }

    private ThrowableFunction<InvocationOnMock, Metrics> normalSaving = (InvocationOnMock invocationOnMock) -> {
        Metrics metrics = invocationOnMock.getArgument(0);
        if(metrics == null) {
            return null;
        }

        //copy metrics
        Metrics newMetrics = copy(metrics);
        newMetrics.setId(METRICS_ID);

        lastConsumedMetric = newMetrics;

        return newMetrics;
    };

    private Metrics copy(Metrics metrics) {
        Metrics newMetrics = new Metrics();

        newMetrics.setMachineId(metrics.getMachineId());
        newMetrics.setCpu(metrics.getCpu());
        newMetrics.setMemory(metrics.getMemory());
        newMetrics.setMetricsDate(metrics.getMetricsDate());

        return newMetrics;
    }
}
