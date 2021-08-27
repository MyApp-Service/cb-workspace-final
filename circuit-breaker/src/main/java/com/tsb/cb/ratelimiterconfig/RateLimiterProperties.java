package com.tsb.cb.ratelimiterconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.ratelimiter")
public class RateLimiterProperties extends RateLimiterConfigurationProperties {

}
