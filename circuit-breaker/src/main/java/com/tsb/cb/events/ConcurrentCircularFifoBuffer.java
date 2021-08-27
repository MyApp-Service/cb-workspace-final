package com.tsb.cb.events;

import java.util.Arrays;

import io.vavr.collection.List;
import io.vavr.control.Option;

public class ConcurrentCircularFifoBuffer <T> implements CircularFifoBuffer<T> {

    private final ConcurrentEvictingQueue<T> queue;
    private final int capacity;

    /**
     * Creates an {@code ConcurrentCircularFifoBuffer} with the given (fixed) capacity
     *
     * @param capacity the capacity of this {@code ConcurrentCircularFifoBuffer}
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ConcurrentCircularFifoBuffer(int capacity) {
        this.capacity = capacity;
        queue = new ConcurrentEvictingQueue<>(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFull() {
        return queue.size() == capacity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> toList() {
    	
		/*
		 * T[] elements = new T[size];
		 * 
		 * for(T c : queue) {
		 * 
		 * }
		 */
    	///T[] elementsArray = (T[]) queue.toArray();
        return (List<T>) List.ofAll(Arrays.asList(queue.toArray()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(T element) {
        queue.offer(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option<T> take() {
        return Option.of(queue.poll());
    }
}