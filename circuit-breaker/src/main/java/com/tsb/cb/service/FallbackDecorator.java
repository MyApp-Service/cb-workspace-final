package com.tsb.cb.service;

import io.vavr.CheckedFunction0;


public interface FallbackDecorator {

	 boolean supports(Class<?> target);

	    /**
	     * @param fallbackMethod fallback method.
	     * @param supplier       target function should be decorated.
	     * @return decorated function
	     */
	    CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod,
	        CheckedFunction0<Object> supplier);
}
