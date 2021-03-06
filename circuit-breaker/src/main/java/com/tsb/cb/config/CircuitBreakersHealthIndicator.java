package com.tsb.cb.config;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.tsb.cb.config.CircuitBreakerConfigurationProperties.InstanceProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class CircuitBreakersHealthIndicator implements HealthIndicator {

    private static final String FAILURE_RATE = "failureRate";
    private static final String SLOW_CALL_RATE = "slowCallRate";
    private static final String FAILURE_RATE_THRESHOLD = "failureRateThreshold";
    private static final String SLOW_CALL_RATE_THRESHOLD = "slowCallRateThreshold";
    private static final String BUFFERED_CALLS = "bufferedCalls";
    private static final String FAILED_CALLS = "failedCalls";
    private static final String SLOW_CALLS = "slowCalls";
    private static final String SLOW_FAILED_CALLS = "slowFailedCalls";
    private static final String NOT_PERMITTED = "notPermittedCalls";
    private static final String STATE = "state";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CircuitBreakerConfigurationProperties circuitBreakerProperties;
    private final HealthAggregator healthAggregator;

    public CircuitBreakersHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry,
                                          CircuitBreakerConfigurationProperties circuitBreakerProperties,
                                          HealthAggregator healthAggregator) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.healthAggregator = healthAggregator;
    }

    private static Health.Builder addDetails(Health.Builder builder,
                                             CircuitBreaker circuitBreaker) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
        builder.withDetail(FAILURE_RATE, metrics.getFailureRate() + "%")
            .withDetail(FAILURE_RATE_THRESHOLD, config.getFailureRateThreshold() + "%")
            .withDetail(SLOW_CALL_RATE, metrics.getSlowCallRate() + "%")
            .withDetail(SLOW_CALL_RATE_THRESHOLD, config.getSlowCallRateThreshold() + "%")
            .withDetail(BUFFERED_CALLS, metrics.getNumberOfBufferedCalls())
            .withDetail(SLOW_CALLS, metrics.getNumberOfSlowCalls())
            .withDetail(SLOW_FAILED_CALLS, metrics.getNumberOfSlowFailedCalls())
            .withDetail(FAILED_CALLS, metrics.getNumberOfFailedCalls())
            .withDetail(NOT_PERMITTED, metrics.getNumberOfNotPermittedCalls())
            .withDetail(STATE, circuitBreaker.getState());
        return builder;
    }

    private boolean allowHealthIndicatorToFail(CircuitBreaker circuitBreaker) {
        return circuitBreakerProperties.findCircuitBreakerProperties(circuitBreaker.getName())
            .map(InstanceProperties::getAllowHealthIndicatorToFail)
            .orElse(false);
    }

    private Health mapBackendMonitorState(CircuitBreaker circuitBreaker) {
        switch (circuitBreaker.getState()) {
            case CLOSED:
                return addDetails(Health.up(), circuitBreaker).build();
            case OPEN:
                boolean allowHealthIndicatorToFail = allowHealthIndicatorToFail(circuitBreaker);

                return addDetails(allowHealthIndicatorToFail ? Health.down() : Health.status("CIRCUIT_OPEN"), circuitBreaker).build();
            case HALF_OPEN:
                return addDetails(Health.status("CIRCUIT_HALF_OPEN"), circuitBreaker).build();
            default:
                return addDetails(Health.unknown(), circuitBreaker).build();
        }
    }

    private boolean isRegisterHealthIndicator(CircuitBreaker circuitBreaker) {
        return circuitBreakerProperties.findCircuitBreakerProperties(circuitBreaker.getName())
            .map(InstanceProperties::getRegisterHealthIndicator)
            .orElse(false);
    }

    @Override
    public Health health() {
        Map<String, Health> healths = circuitBreakerRegistry.getAllCircuitBreakers().toJavaStream()
            .filter(this::isRegisterHealthIndicator)
            .collect(Collectors.toMap(CircuitBreaker::getName,
                this::mapBackendMonitorState));

        return healthAggregator.aggregate(healths);
    }
}
