package com.aiven.metrics.systems;

import org.mockito.invocation.InvocationOnMock;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

@FunctionalInterface
public interface ThrowableKafkaTemplateFunction<KEY, VALUE> {
    ListenableFuture<SendResult<KEY, VALUE>> apply(InvocationOnMock var1) throws Throwable;
}