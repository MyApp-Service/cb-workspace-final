package com.tsb.cb.service;

import org.aspectj.lang.ProceedingJoinPoint;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public interface CircuitBreakerAspectExt {
	
	boolean canHandleReturnType(Class returnType);

    Object handle(ProceedingJoinPoint proceedingJoinPoint,
        CircuitBreaker circuitBreaker, String methodName) throws Throwable;

}
