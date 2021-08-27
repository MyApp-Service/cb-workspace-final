package com.tsb.cb.events;

import io.github.resilience4j.core.EventConsumer;
import io.vavr.collection.List;

public class CircularEventConsumer<T> implements EventConsumer<T> {

    private final CircularFifoBuffer<T> eventCircularFifoBuffer;

    /**
     * Creates an {@code CircuitBreakerEventConsumer} with the given (fixed) capacity
     *
     * @param capacity the capacity of this CircuitBreakerEventConsumer
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public CircularEventConsumer(int capacity) {
        this.eventCircularFifoBuffer = new ConcurrentCircularFifoBuffer<>(capacity);
    }

    @Override
    public void consumeEvent(T event) {
        eventCircularFifoBuffer.add(event);
    }

    /**
     * Returns a list containing all of the buffered events.
     *
     * @return a list containing all of the buffered events.
     */
    public List<T> getBufferedEvents() {
        return eventCircularFifoBuffer.toList();
    }
}
