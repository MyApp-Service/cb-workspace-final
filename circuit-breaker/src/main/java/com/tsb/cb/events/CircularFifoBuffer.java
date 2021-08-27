package com.tsb.cb.events;

import io.vavr.collection.List;
import io.vavr.control.Option;

public interface CircularFifoBuffer<T> {

	/**
     * Returns the number of elements in this {@link CircularFifoBuffer}.
     *
     * @return the number of elements in this {@link CircularFifoBuffer}
     */
    int size();

    /**
     * Returns <code>true</code> if this {@link CircularFifoBuffer} contains no elements.
     *
     * @return <code>true</code> if this {@link CircularFifoBuffer} contains no elements
     */
    boolean isEmpty();

    /**
     * Returns <code>true</code> if this {@link CircularFifoBuffer} is full.
     *
     * @return <code>true</code> if this {@link CircularFifoBuffer} is full
     */
    boolean isFull();

    /**
     * Returns a list containing all of the elements in this {@link CircularFifoBuffer}. The
     * elements are copied into an array.
     *
     * @return a list containing all of the elements in this {@link CircularFifoBuffer}
     */
    List<T> toList();

    /**
     * Adds element to the {@link CircularFifoBuffer} and overwrites the oldest element when {@link
     * CircularFifoBuffer#isFull}.
     *
     * @param element to add
     * @throws NullPointerException if the specified element is null
     */
    void add(T element);

    /**
     * Retrieves and removes the head of this queue, or returns {@link Option.None} if this queue is
     * empty.
     *
     * @return the head of this queue, or {@link Option.None} if this queue is empty
     */
    Option<T> take();
}
