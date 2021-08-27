package com.tsb.cb.timelimiterconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.timelimiter")
public class TimeLimiterProperties extends TimeLimiterConfigurationProperties {
}

