package com.tsb.cb.ratelimiterconfig;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import com.tsb.cb.ratelimiterconfig.RateLimiterConfigurationProperties.InstanceProperties;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;

public class RateLimitersHealthIndicator implements HealthIndicator {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfigurationProperties rateLimiterProperties;
    private final StatusAggregator statusAggregator;

    public RateLimitersHealthIndicator(RateLimiterRegistry rateLimiterRegistry,
        RateLimiterConfigurationProperties rateLimiterProperties,
        StatusAggregator statusAggregator) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.rateLimiterProperties = rateLimiterProperties;
        this.statusAggregator = statusAggregator;
    }

    private static Health rateLimiterHealth(Status status, int availablePermissions,
        int numberOfWaitingThreads) {
        return Health.status(status)
            .withDetail("availablePermissions", availablePermissions)
            .withDetail("numberOfWaitingThreads", numberOfWaitingThreads)
            .build();
    }

    @Override
    public Health health() {
        Map<String, Health> healths = rateLimiterRegistry.getAllRateLimiters().toJavaStream()
            .filter(this::isRegisterHealthIndicator)
            .collect(Collectors.toMap(RateLimiter::getName, this::mapRateLimiterHealth));

        Status status = statusAggregator.getAggregateStatus(healths.values().stream().map(Health::getStatus).collect(Collectors.toSet()));
        return Health.status(status).build();//withDetails(healths).build();
    }

    private boolean isRegisterHealthIndicator(RateLimiter rateLimiter) {
        return rateLimiterProperties.findRateLimiterProperties(rateLimiter.getName())
            .map(InstanceProperties::getRegisterHealthIndicator)
            .orElse(false);
    }

    private boolean allowHealthIndicatorToFail(RateLimiter rateLimiter) {
        return rateLimiterProperties.findRateLimiterProperties(rateLimiter.getName())
                .map(InstanceProperties::getAllowHealthIndicatorToFail)
                .orElse(false);
    }

    private Health mapRateLimiterHealth(RateLimiter rateLimiter) {
        RateLimiter.Metrics metrics = rateLimiter.getMetrics();
        int availablePermissions = metrics.getAvailablePermissions();
        int numberOfWaitingThreads = metrics.getNumberOfWaitingThreads();
        long timeoutInNanos = rateLimiter.getRateLimiterConfig().getTimeoutDuration().toNanos();

        if (availablePermissions > 0 || numberOfWaitingThreads == 0) {
            return rateLimiterHealth(Status.UP, availablePermissions, numberOfWaitingThreads);
        }

        if (rateLimiter instanceof AtomicRateLimiter) {
            AtomicRateLimiter atomicRateLimiter = (AtomicRateLimiter) rateLimiter;
            AtomicRateLimiter.AtomicRateLimiterMetrics detailedMetrics = atomicRateLimiter
                .getDetailedMetrics();
            if (detailedMetrics.getNanosToWait() > timeoutInNanos) {
                boolean allowHealthIndicatorToFail = allowHealthIndicatorToFail(rateLimiter);

                return rateLimiterHealth(allowHealthIndicatorToFail ? Status.DOWN : new Status("RATE_LIMITED"), availablePermissions, numberOfWaitingThreads);
            }
        }
        return rateLimiterHealth(Status.UNKNOWN, availablePermissions, numberOfWaitingThreads);
    }
}

