package com.tsb.cb.events;

import io.github.resilience4j.core.lang.Nullable;
import io.vavr.collection.Seq;

public interface EventConsumerRegistry<T> {

	 /**
     * Creates a new EventConsumer and stores the instance in the registry.
     *
     * @param id         the id of the EventConsumer
     * @param bufferSize the size of the EventConsumer
     * @return a new EventConsumer
     */
    CircularEventConsumer<T> createEventConsumer(String id, int bufferSize);

    /**
     * Returns the EventConsumer to which the specified id is mapped.
     *
     * @param id the id of the EventConsumer
     * @return the EventConsumer to which the specified id is mapped
     */
    @Nullable
    CircularEventConsumer<T> getEventConsumer(String id);

    /**
     * Returns all EventConsumer instances.
     *
     * @return all EventConsumer instances
     */
    Seq<CircularEventConsumer<T>> getAllEventConsumer();
}
