package com.tsb.cb.bulkheadconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resilience4j.bulkhead")
public class BulkheadProperties extends BulkheadConfigurationProperties {

}
