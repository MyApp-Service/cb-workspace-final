package com.tsb.cb.events;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

@Component
public class DefaultEventConsumerRegistry<T> implements EventConsumerRegistry<T> {

    /**
     * The CircularEventConsumers, indexed by name of the backend.
     */
    private final ConcurrentMap<String, CircularEventConsumer<T>> registry= new ConcurrentHashMap<>();

    /**
     * The constructor with default circuitBreaker properties.
     */
	/*
	 * public DefaultEventConsumerRegistry() { this.registry = new
	 * ConcurrentHashMap<>(); }
	 */

    @Override
    public CircularEventConsumer<T> createEventConsumer(String id, int bufferSize) {
        CircularEventConsumer<T> eventConsumer = new CircularEventConsumer<>(bufferSize);
        registry.put(id, eventConsumer);
        return eventConsumer;
    }

    @Override
    public CircularEventConsumer<T> getEventConsumer(String id) {
        return registry.get(id);
    }

    @Override
    public Seq<CircularEventConsumer<T>> getAllEventConsumer() {
        return Array.ofAll(registry.values());
    }
}
 
