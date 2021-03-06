package com.tsb.cb.bulkheadconfig;

import java.util.function.Consumer;

import com.tsb.cb.config.CustomizerWithName;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.core.lang.NonNull;

public interface BulkheadConfigCustomizer extends CustomizerWithName {

    /**
     * Customize BulkheadConfig configuration builder.
     *
     * @param configBuilder to be customized
     */
    void customize(BulkheadConfig.Builder configBuilder);

    /**
     * A convenient method to create BulkheadConfigCustomizer using {@link Consumer}
     *
     * @param instanceName the name of the instance
     * @param consumer     delegate call to Consumer when  {@link BulkheadConfigCustomizer#customize(BulkheadConfig.Builder)}
     *                     is called
     * @return Customizer instance
     */
    static  BulkheadConfigCustomizer of(@NonNull String instanceName,
        @NonNull Consumer<BulkheadConfig.Builder> consumer) {
        return new BulkheadConfigCustomizer() {

            @Override
            public void customize(BulkheadConfig.Builder builder) {
                consumer.accept(builder);
            }

            @Override
            public String name() {
                return instanceName;
            }
        };
    }
}
