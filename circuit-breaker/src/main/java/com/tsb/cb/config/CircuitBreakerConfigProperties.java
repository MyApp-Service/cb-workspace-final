package com.tsb.cb.config;

import org.springframework.core.Ordered;

public class CircuitBreakerConfigProperties extends CircuitBreakerConfigurationProperties{

	private int circuitBreakerAspectOrder = Ordered.LOWEST_PRECEDENCE - 3;

	public int getCircuitBreakerAspectOrder() {
		return circuitBreakerAspectOrder;
	}

	public void setCircuitBreakerAspectOrder(int circuitBreakerAspectOrder) {
		this.circuitBreakerAspectOrder = circuitBreakerAspectOrder;
	}
}
