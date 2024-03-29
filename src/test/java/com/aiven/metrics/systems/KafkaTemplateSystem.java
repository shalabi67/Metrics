package com.aiven.metrics.systems;

import com.aiven.metrics.model.Metrics;
import com.aiven.metrics.model.MetricsRetry;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * This class provides a general way to create a mock for KafkaTemplate.
 * It will mock send method and identify the send method implementation by passing a FunctionInterface Function
 * @param <KEY> key type
 * @param <VALUE> value type
 * @see Function
 * @see KafkaTemplate
 * @see FunctionalInterface
 */
@Getter
@Setter
public class KafkaTemplateSystem<KEY, VALUE> {
    private Consumer<VALUE> consumer;



    public KafkaTemplate<KEY, VALUE> createKafkaTemplate(
            ThrowableKafkaTemplateFunction<KEY, VALUE> function) {
        KafkaTemplate<KEY, VALUE> kafkaTemplate = (KafkaTemplate<KEY, VALUE>) Mockito.mock(KafkaTemplate.class);
        if(kafkaTemplate == null) {
            Assertions.fail();
        }

        Mockito.when(kafkaTemplate.send(anyString(), any())).thenAnswer(
                new Answer<ListenableFuture<SendResult<KEY, VALUE>>>() {
                    @Override
                    public ListenableFuture<SendResult<KEY, VALUE>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return function.apply(invocationOnMock);
                    }
                });

        return kafkaTemplate;
    }

    private ThrowableKafkaTemplateFunction<KEY, VALUE> defaultKafkaSend = (invocationOnMock -> {
        VALUE metrics = invocationOnMock.getArgument(1);
        String topic = invocationOnMock.getArgument(0);
        if(consumer != null) {
            consumer.accept(metrics);
        }
        SettableListenableFuture<SendResult<KEY, VALUE>> future = new SettableListenableFuture<>();
        future.set(new SendResult<KEY, VALUE>(new ProducerRecord<>(topic, metrics), null));
        return future;
    });
}
