package com.tsb.cb.endpoint;

import javax.websocket.ClientEndpoint;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@ClientEndpoint()
public class CircuitBreakerEndpoint{
	
	private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerEndpoint(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
}