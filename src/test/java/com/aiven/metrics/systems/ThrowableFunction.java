package com.aiven.metrics.systems;

@FunctionalInterface
public interface ThrowableFunction<PARAM, RESULT> {
    RESULT apply(PARAM parameter) throws Throwable;
}