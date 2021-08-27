package com.tsb.cb.bulkheadconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.thread-pool-bulkhead")
public class ThreadPoolBulkheadProperties extends ThreadPoolBulkheadConfigurationProperties {

}
