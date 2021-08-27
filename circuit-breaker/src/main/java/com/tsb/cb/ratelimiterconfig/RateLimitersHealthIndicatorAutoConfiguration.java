package com.tsb.cb.ratelimiterconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

public class RateLimitersHealthIndicatorAutoConfiguration {

	@Bean
    @ConditionalOnMissingBean(name = "rateLimitersHealthIndicator")
    @ConditionalOnProperty(prefix = "management.health.ratelimiters", name = "enabled")
    public RateLimitersHealthIndicator rateLimitersHealthIndicator(
        RateLimiterRegistry rateLimiterRegistry,
        RateLimiterConfigurationProperties rateLimiterProperties,
        StatusAggregator statusAggregator) {
        return new RateLimitersHealthIndicator(rateLimiterRegistry, rateLimiterProperties, statusAggregator);
    }
}
