package com.tsb.cb.retrycore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;


public interface ContextPropagator<T> {

	
	Supplier<Optional<T>> retrieve();

    /**
     * Copies value from the parent thread into new executing thread. This method is passed with the
     * values received from method {@link ContextPropagator#retrieve()} in the parent thread.
     *
     * @return a Consumer to set values in new thread.
     */
    Consumer<Optional<T>> copy();

    /**
     * CleanUp value before thread execution finish. This method is passed with the values received
     * from method {@link ContextPropagator#retrieve()} in the parent thread.
     *
     * @return a Consumer to cleanUp values.
     */
    Consumer<Optional<T>> clear();

    /**
     * Method decorates supplier to copy variables across thread boundary.
     *
     * @param propagator the instance of {@link ContextPropagator}
     * @param supplier   the supplier to be decorated
     * @param <T>        the type of variable that cross thread boundary
     * @return decorated supplier of type T
     */
    static <T> Supplier<T> decorateSupplier(ContextPropagator propagator,
                                            Supplier<T> supplier) {
        final Optional value = (Optional) propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                return supplier.get();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    /**
     * Method decorates supplier to copy variables across thread boundary.
     *
     * @param propagators the instance of {@link ContextPropagator} should be non null.
     * @param supplier    the supplier to be decorated
     * @param <T>         the type of variable that cross thread boundary
     * @return decorated supplier of type T
     */
    static <T> Supplier<T> decorateSupplier(List<? extends ContextPropagator> propagators,
                                            Supplier<T> supplier) {

        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");

        //Create identity map of <ContextPropagator,Optional Supplier value>, if we have duplicate ContextPropagators then last one wins.
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
            .collect(toMap(
                p -> p, //key as ContextPropagator instance itself
                p -> p.retrieve().get(), //Supplier Optional value
                (first, second) -> second, //Merge function, this simply choose later value in key collision
                HashMap::new)); //type of map

        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                return supplier.get();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * Method decorates callable to copy variables across thread boundary.
     *
     * @param propagator the instance of {@link ContextPropagator}
     * @param callable   the callable to be decorated
     * @param <T>        the type of variable that cross thread boundary
     * @return decorated callable of type T
     */
    static <T> Callable<T> decorateCallable(ContextPropagator propagator, Callable<T> callable) {
        final Optional value = (Optional) propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                return callable.call();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    /**
     * Method decorates callable to copy variables across thread boundary.
     *
     * @param propagators the instance of {@link ContextPropagator} should be non null.
     * @param callable    the callable to be decorated
     * @param <T>         the type of variable that cross thread boundary
     * @return decorated callable of type T
     */
    static <T> Callable<T> decorateCallable(List<? extends ContextPropagator> propagators,
                                            Callable<T> callable) {

        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");

        //Create identity map of <ContextPropagator,Optional Supplier value>, if we have duplicate ContextPropagators then last one wins.
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
            .collect(toMap(
                p -> p, //key as ContextPropagator instance itself
                p -> p.retrieve().get(), //Supplier Optional value
                (first, second) -> second, //Merge function, this simply choose later value in key collision
                HashMap::new)); //type of map

        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                return callable.call();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * Method decorates runnable to copy variables across thread boundary.
     *
     * @param propagators the instance of {@link ContextPropagator}
     * @param runnable    the runnable to be decorated
     * @param <T>         the type of variable that cross thread boundary
     * @return decorated supplier of type T
     */
    static <T> Runnable decorateRunnable(List<? extends ContextPropagator> propagators,
                                         Runnable runnable) {
        Objects.requireNonNull(propagators, "ContextPropagator list should be non null");

        //Create identity map of <ContextPropagator,Optional Supplier value>, if we have duplicate ContextPropagators then last one wins.
        final Map<? extends ContextPropagator, Object> values = propagators.stream()
            .collect(toMap(
                p -> p, //key as ContextPropagator instance itself
                p -> p.retrieve().get(), //Supplier Optional value
                (first, second) -> second, //Merge function, this simply choose later value in key collision
                HashMap::new)); //type of map

        return () -> {
            try {
                values.forEach((p, v) -> p.copy().accept(v));
                runnable.run();
            } finally {
                values.forEach((p, v) -> p.clear().accept(v));
            }
        };
    }

    /**
     * Method decorates runnable to copy variables across thread boundary.
     *
     * @param propagator the instance of {@link ContextPropagator}
     * @param runnable   the runnable to be decorated
     * @param <T>        the type of variable that cross thread boundary
     * @return decorated supplier of type T
     */
    static <T> Runnable decorateRunnable(ContextPropagator propagator,
                                         Runnable runnable) {
        final Optional value = (Optional) propagator.retrieve().get();
        return () -> {
            try {
                propagator.copy().accept(value);
                runnable.run();
            } finally {
                propagator.clear().accept(value);
            }
        };
    }

    /**
     * An empty context propagator.
     *
     * @param <T> type.
     * @return an empty {@link ContextPropagator}
     */
    static <T> ContextPropagator<T> empty() {
        return new EmptyContextPropagator<>();
    }

    /**
     * A convenient implementation of empty {@link ContextPropagator}
     *
     * @param <T> type of class.
     */
    class EmptyContextPropagator<T> implements ContextPropagator<T> {

        @Override
        public Supplier<Optional<T>> retrieve() {
            return () -> Optional.empty();
        }

        @Override
        public Consumer<Optional<T>> copy() {
            return (t) -> {
            };
        }

        @Override
        public Consumer<Optional<T>> clear() {
            return (t) -> {
            };
        }
    }
}
