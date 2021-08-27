package com.tsb.cb.config;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

@Component
public class ResilienceConfig {
	
	

	public void processConfig(String name) {
		
		// Create a custom configuration for a CircuitBreaker
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
		    .failureRateThreshold(50)
		    .waitDurationInOpenState(Duration.ofMillis(1000))
		    .permittedNumberOfCallsInHalfOpenState(2)
		    .slidingWindowSize(2)
		    .recordExceptions(IOException.class, TimeoutException.class)
		    .build();

		// Create a CircuitBreakerRegistry with a custom global configuration
		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		
		
		
		CircuitBreaker cb = registry.circuitBreaker(name);
		
		Supplier<String> supplier = CircuitBreaker.decorateSupplier(cb, ResilienceConfig::showError);
		
		String result = Try.ofSupplier(supplier)
			    .recover(throwable -> "Hello from Recovery").get();
		
		System.out.println(result);
	}
	
	public static String showError() {
		if(false) {
			throw new RuntimeException("This has Error Please try again...");
		}
		return "Hello... from error";
	}
	
}
