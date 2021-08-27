package com.tsb.cb.timelimiterconfig;

import java.util.function.Consumer;

import com.tsb.cb.config.CustomizerWithName;

import io.github.resilience4j.core.lang.NonNull;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

public interface TimeLimiterConfigCustomizer extends CustomizerWithName {

    /**
     * Customize time limiter configuration builder.
     *
     * @param configBuilder to be customized
     */
    void customize(TimeLimiterConfig.Builder configBuilder);

    /**
     * A convenient method to create TimeLimiterConfigCustomizer using {@link Consumer}
     *
     * @param instanceName the name of the instance
     * @param consumer     delegate call to Consumer when  {@link TimeLimiterConfigCustomizer#customize(TimeLimiterConfig.Builder)}
     *                     is called
     * @return Customizer instance
     */
    static TimeLimiterConfigCustomizer of(@NonNull String instanceName,
        @NonNull Consumer<TimeLimiterConfig.Builder> consumer) {
        return new TimeLimiterConfigCustomizer() {

            @Override
            public void customize(TimeLimiterConfig.Builder builder) {
                consumer.accept(builder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }

}

