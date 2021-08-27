package com.tsb.cb.utils;

import java.lang.reflect.Constructor;

import com.tsb.cb.function.IntervalBiFunction;
import io.github.resilience4j.core.InstantiationException;

public final class TSBClassUtils {
	
	private static final String INSTANTIATION_ERROR_PREFIX = "Unable to create instance of class: ";

	private TSBClassUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static <T> IntervalBiFunction<T> instantiateIntervalBiFunctionClass(
	        Class<? extends IntervalBiFunction<T>> clazz) {
	        try {
	            Constructor<? extends IntervalBiFunction<T>> c = clazz.getConstructor();
	            if (c != null) {
	                return c.newInstance();
	            } else {
	                throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName());
	            }
	        } catch (Exception e) {
	            throw new InstantiationException(INSTANTIATION_ERROR_PREFIX + clazz.getName(), e);
	        }
	    }
}
