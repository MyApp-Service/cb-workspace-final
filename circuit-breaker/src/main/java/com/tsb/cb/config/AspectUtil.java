package com.tsb.cb.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.context.annotation.ConditionContext;

import static java.util.Objects.requireNonNull;


public class AspectUtil {


    private AspectUtil() {
    }

    /**
     * @param context           the spring condition context
     * @param classToCheck      the class to check in spring class loader
     * @param exceptionConsumer the custom exception consumer
     * @return true or false if the class is found or not
     */
    static boolean checkClassIfFound(ConditionContext context, String classToCheck,
        Consumer<Exception> exceptionConsumer) {
        try {
            final Class<?> aClass = requireNonNull(context.getClassLoader(),
                "context must not be null").loadClass(classToCheck);
            return aClass != null;
        } catch (ClassNotFoundException e) {
            exceptionConsumer.accept(e);
            return false;
        }
    }

    @SafeVarargs
    public static <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, objs);
        return Collections.unmodifiableSet(set);
    }
}
