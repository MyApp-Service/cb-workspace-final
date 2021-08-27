package com.tsb.cb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface TSBBulkhead {

	/**
     * Name of the bulkhead.
     * It can be SpEL expression. If you want to use first parameter of the method as name, you can
     * express it {@code #root.args[0]}, {@code #p0} or {@code #a0}. And method name can be accessed via
     * {@code #root.methodName}
     *
     * @return the name of the bulkhead
     */
    String name();

    /**
     * fallbackMethod method name.
     *
     * @return fallbackMethod method name.
     */
    String fallback() default "";

    /**
     * @return the bulkhead implementation type (SEMAPHORE or THREADPOOL)
     */
    Type type() default Type.SEMAPHORE;

    /**
     * bulkhead implementation types
     * <p>
     * SEMAPHORE will invoke semaphore based bulkhead implementation THREADPOOL will invoke Thread
     * pool based bulkhead implementation
     */
    enum Type {
        SEMAPHORE, THREADPOOL
    }
	
}
