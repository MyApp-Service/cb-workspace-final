package com.tsb.cb.config;

import com.tsb.cb.bulkheadconfig.BulkheadConfigurationProperties;
import com.tsb.cb.ratelimiterconfig.RateLimiterConfigurationProperties;
import com.tsb.cb.retryconfig.RetryConfigurationProperties;
import com.tsb.cb.timelimiterconfig.TimeLimiterConfigurationProperties;

public class ConfigUtils {

	private ConfigUtils() {
    }

    /**
     * merge only properties that are not part of retry config if any match the conditions of merge
     *
     * @param baseProperties     base config properties
     * @param instanceProperties instance properties
     */
    public static void mergePropertiesIfAny(
        CircuitBreakerConfigurationProperties.InstanceProperties instanceProperties,
        CircuitBreakerConfigurationProperties.InstanceProperties baseProperties) {
        if (instanceProperties.getRegisterHealthIndicator() == null &&
            baseProperties.getRegisterHealthIndicator() != null) {
            instanceProperties.setRegisterHealthIndicator(baseProperties.getRegisterHealthIndicator());
        }
        if (instanceProperties.getAllowHealthIndicatorToFail() == null &&
            baseProperties.getAllowHealthIndicatorToFail() != null) {
            instanceProperties.setAllowHealthIndicatorToFail(baseProperties.getAllowHealthIndicatorToFail());
        }
        if (instanceProperties.getEventConsumerBufferSize() == null &&
            baseProperties.getEventConsumerBufferSize() != null) {
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
        }
    }

    /**
     * merge only properties that are not part of retry config if any match the conditions of merge
     *
     * @param baseProperties     base config properties
     * @param instanceProperties instance properties
     */
    public static void mergePropertiesIfAny(
        BulkheadConfigurationProperties.InstanceProperties baseProperties,
        BulkheadConfigurationProperties.InstanceProperties instanceProperties) {
        if (instanceProperties.getEventConsumerBufferSize() == null &&
            baseProperties.getEventConsumerBufferSize() != null) {
                instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
        }
    }

    /**
     * merge only properties that are not part of retry config if any match the conditions of merge
     *
     * @param baseProperties     base config properties
     * @param instanceProperties instance properties
     */
    public static void mergePropertiesIfAny(
        RateLimiterConfigurationProperties.InstanceProperties baseProperties,
        RateLimiterConfigurationProperties.InstanceProperties instanceProperties) {
        if (instanceProperties.getRegisterHealthIndicator() == null &&
            baseProperties.getRegisterHealthIndicator() != null) {
            instanceProperties.setRegisterHealthIndicator(baseProperties.getRegisterHealthIndicator());
        }
        if (instanceProperties.getAllowHealthIndicatorToFail() == null &&
            baseProperties.getAllowHealthIndicatorToFail() != null) {
            instanceProperties.setAllowHealthIndicatorToFail(baseProperties.getAllowHealthIndicatorToFail());
        }
        if (instanceProperties.getSubscribeForEvents() == null &&
            baseProperties.getSubscribeForEvents() != null) {
            instanceProperties.setSubscribeForEvents(baseProperties.getSubscribeForEvents());
        }
        if (instanceProperties.getEventConsumerBufferSize() == null &&
            baseProperties.getEventConsumerBufferSize() != null) {
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
        }
    }

    /**
     * merge only properties that are not part of retry config if any match the conditions of merge
     *
     * @param baseProperties     base config properties
     * @param instanceProperties instance properties
     */
    public static void mergePropertiesIfAny(
        RetryConfigurationProperties.InstanceProperties baseProperties,
        RetryConfigurationProperties.InstanceProperties instanceProperties) {
        if (instanceProperties.getEnableExponentialBackoff() == null &&
            baseProperties.getEnableExponentialBackoff() != null) {
            instanceProperties.setEnableExponentialBackoff(baseProperties.getEnableExponentialBackoff());
        }
        if (instanceProperties.getEnableRandomizedWait() == null &&
            baseProperties.getEnableRandomizedWait() != null) {
            instanceProperties.setEnableRandomizedWait(baseProperties.getEnableRandomizedWait());
        }
        if (instanceProperties.getExponentialBackoffMultiplier() == null &&
            baseProperties.getExponentialBackoffMultiplier() != null) {
            instanceProperties.setExponentialBackoffMultiplier(baseProperties.getExponentialBackoffMultiplier());
        }
        if (instanceProperties.getExponentialMaxWaitDuration() == null &&
            baseProperties.getExponentialMaxWaitDuration() != null) {
            instanceProperties.setExponentialMaxWaitDuration(baseProperties.getExponentialMaxWaitDuration());
        }
    }

	/**
	 * merge only properties that are not part of timeLimiter config if any match the conditions of merge
	 *
	 * @param baseProperties     base config properties
	 * @param instanceProperties instance properties
	 */
	public static void mergePropertiesIfAny(TimeLimiterConfigurationProperties.InstanceProperties baseProperties,
											TimeLimiterConfigurationProperties.InstanceProperties instanceProperties) {
		if (instanceProperties.getEventConsumerBufferSize() == null
            && baseProperties.getEventConsumerBufferSize() != null) {
            instanceProperties.setEventConsumerBufferSize(baseProperties.getEventConsumerBufferSize());
		}
	}
}
