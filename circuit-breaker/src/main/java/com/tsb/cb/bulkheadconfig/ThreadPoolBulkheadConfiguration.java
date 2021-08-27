package com.tsb.cb.bulkheadconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.bulkheadconfig.ThreadPoolBulkheadConfigurationProperties.InstanceProperties;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.core.registry.CompositeRegistryEventConsumer;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

import static java.util.Optional.ofNullable;

@Configuration
public class ThreadPoolBulkheadConfiguration {

	@Bean
    @Qualifier("compositeThreadPoolBulkheadCustomizer")
    public CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer(
        List<ThreadPoolBulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    /**
     * @param bulkheadConfigurationProperties bulk head spring configuration properties
     * @param bulkheadEventConsumerRegistry   the bulk head event consumer registry
     * @return the ThreadPoolBulkheadRegistry with all needed setup in place
     */
    @Bean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties,
        EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
        RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
			/* @Qualifier("compositeThreadPoolBulkheadCustomizer") */ CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        ThreadPoolBulkheadRegistry bulkheadRegistry = createBulkheadRegistry(
            bulkheadConfigurationProperties, threadPoolBulkheadRegistryEventConsumer,
            compositeThreadPoolBulkheadCustomizer);
        registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry,
            bulkheadConfigurationProperties);
        bulkheadConfigurationProperties.getBackends().forEach((name, properties) -> bulkheadRegistry
            .bulkhead(name, bulkheadConfigurationProperties
                .createThreadPoolBulkheadConfig(name, compositeThreadPoolBulkheadCustomizer)));
        return bulkheadRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<ThreadPoolBulkhead>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(
            optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    /**
     * Initializes a bulkhead registry.
     *
     * @param threadPoolBulkheadConfigurationProperties The bulkhead configuration properties.
     * @param compositeThreadPoolBulkheadCustomizer the delegate of customizers
     * @return a ThreadPoolBulkheadRegistry
     */
    private ThreadPoolBulkheadRegistry createBulkheadRegistry(
        ThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
        RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
        CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        Map<String, ThreadPoolBulkheadConfig> configs = threadPoolBulkheadConfigurationProperties
            .getConfigs()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                entry -> threadPoolBulkheadConfigurationProperties
                    .createThreadPoolBulkheadConfig(entry.getValue(),
                        compositeThreadPoolBulkheadCustomizer, entry.getKey())));
        return ThreadPoolBulkheadRegistry.of(configs, threadPoolBulkheadRegistryEventConsumer,
            io.vavr.collection.HashMap.ofAll(threadPoolBulkheadConfigurationProperties.getTags()));
    }

    /**
     * Registers the post creation consumer function that registers the consumer events to the
     * bulkheads.
     *
     * @param bulkheadRegistry      The BulkHead registry.
     * @param eventConsumerRegistry The event consumer registry.
     */
    private void registerEventConsumer(ThreadPoolBulkheadRegistry bulkheadRegistry,
        EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
        ThreadPoolBulkheadConfigurationProperties properties) {
        bulkheadRegistry.getEventPublisher()
            .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
            .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
        ThreadPoolBulkhead bulkHead,
        ThreadPoolBulkheadConfigurationProperties bulkheadConfigurationProperties) {
        int eventConsumerBufferSize = ofNullable(bulkheadConfigurationProperties.getBackendProperties(bulkHead.getName()))
            .map(InstanceProperties::getEventConsumerBufferSize)
            .orElse(100);
        bulkHead.getEventPublisher().onEvent(eventConsumerRegistry.createEventConsumer(
            String.join("-", ThreadPoolBulkhead.class.getSimpleName(), bulkHead.getName()),
            eventConsumerBufferSize));
    }
	
}
