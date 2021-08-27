package com.tsb.cb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
public class CircuitBreakerProperties  extends CircuitBreakerConfigProperties{

}
