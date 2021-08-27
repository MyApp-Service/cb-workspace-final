package com.tsb.cb.ratelimiterconfig;

import java.util.function.Consumer;

import com.tsb.cb.config.CustomizerWithName;

import io.github.resilience4j.core.lang.NonNull;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

public interface RateLimiterConfigCustomizer extends CustomizerWithName {

    /**
     * Customize rate limiter configuration builder.
     *
     * @param configBuilder to be customized
     */
    void customize(RateLimiterConfig.Builder configBuilder);

    /**
     * A convenient method to create RateLimiterConfigCustomizer using {@link Consumer}
     *
     * @param instanceName the name of the instance
     * @param consumer     delegate call to Consumer when  {@link RateLimiterConfigCustomizer#customize(RateLimiterConfig.Builder)}
     *                     is called
     * @return Customizer instance
     */
    static RateLimiterConfigCustomizer of(@NonNull String instanceName,
        @NonNull Consumer<RateLimiterConfig.Builder> consumer) {
        return new RateLimiterConfigCustomizer() {

            @Override
            public void customize(RateLimiterConfig.Builder builder) {
                consumer.accept(builder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}

