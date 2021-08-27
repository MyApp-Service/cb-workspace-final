package com.tsb.cb.function;

import java.util.function.BiFunction;

import io.github.resilience4j.core.IntervalFunction;
import io.vavr.control.Either;
@FunctionalInterface
public interface IntervalBiFunction<T> extends BiFunction<Integer, Either<Throwable, T>, Long> {

    static <T> IntervalBiFunction<T> ofIntervalFunction(IntervalFunction f) {
        return (attempt, either) -> f.apply(attempt);
    }
}
