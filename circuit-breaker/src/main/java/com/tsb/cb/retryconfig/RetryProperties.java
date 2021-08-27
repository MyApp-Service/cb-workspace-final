package com.tsb.cb.retryconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.retry")
public class RetryProperties extends RetryConfigProperties {


}
