package com.tsb.cb.retryconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBRetryAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.events.DefaultEventConsumerRegistry;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retryconfig.RetryConfigurationProperties.InstanceProperties;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;

import io.github.resilience4j.core.registry.CompositeRegistryEventConsumer;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;

@Configuration
public class RetryConfiguration {
	
	/*
	 * @Autowired RetryConfigCustomizer retryConfigCustomizer;
	 */
	
	@Bean
    @Qualifier("compositeRetryCustomizer")
	public CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer(
        List<RetryConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }
	
	/**
     * @param retryConfigurationProperties retryConfigurationProperties retry configuration spring
     *                                     properties
     * @param retryEventConsumerRegistry   the event retry registry
     * @return the retry definition registry
     */
    @Bean
    public RetryRegistry retryRegistry(RetryConfigurationProperties retryConfigurationProperties,
        EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
        RegistryEventConsumer<Retry> retryRegistryEventConsumer,
			/* @Qualifier("compositeRetryCustomizer") */ CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
        RetryRegistry retryRegistry = createRetryRegistry(retryConfigurationProperties,
            retryRegistryEventConsumer, compositeRetryCustomizer);
        registerEventConsumer(retryRegistry, retryEventConsumerRegistry,
            retryConfigurationProperties);
        retryConfigurationProperties.getInstances()
            .forEach((name, properties) ->
                retryRegistry.retry(name, retryConfigurationProperties
                    .createRetryConfig(name, compositeRetryCustomizer)));
        return retryRegistry;
    }
	
	
	 @Bean
    @Primary
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<Retry>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(
            optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }
	
	/**
     * Initializes a retry registry.
     *
     * @param retryConfigurationProperties The retry configuration properties.
     * @return a RetryRegistry
     */
    private RetryRegistry createRetryRegistry(
        RetryConfigurationProperties retryConfigurationProperties,
        RegistryEventConsumer<Retry> retryRegistryEventConsumer,
        CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
        Map<String, RetryConfig> configs = retryConfigurationProperties.getConfigs()
            .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> retryConfigurationProperties
                    .createRetryConfig(entry.getValue(), compositeRetryCustomizer,
                        entry.getKey())));

        return RetryRegistry.of(configs, retryRegistryEventConsumer,
            io.vavr.collection.HashMap.ofAll(retryConfigurationProperties.getTags()));
    }
	
	/**
     * Registers the post creation consumer function that registers the consumer events to the
     * retries.
     *
     * @param retryRegistry         The retry registry.
     * @param eventConsumerRegistry The event consumer registry.
     */
    private void registerEventConsumer(RetryRegistry retryRegistry,
        EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
        RetryConfigurationProperties properties) {
        retryRegistry.getEventPublisher()
            .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
            .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private void registerEventConsumer(EventConsumerRegistry<RetryEvent> eventConsumerRegistry,
        Retry retry, RetryConfigurationProperties retryConfigurationProperties) {
        int eventConsumerBufferSize = Optional
            .ofNullable(retryConfigurationProperties.getBackendProperties(retry.getName()))
            .map(InstanceProperties::getEventConsumerBufferSize)
            .orElse(100);
        retry.getEventPublisher().onEvent(
            eventConsumerRegistry.createEventConsumer(retry.getName(), eventConsumerBufferSize));
    }
	
	 /**
     * @param retryConfigurationProperties retry configuration spring properties
     * @param retryRegistry                retry in memory registry
     * @return the spring retry AOP aspect
     */
    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    public TSBRetryAop retryAspect(
        RetryConfigProperties retryConfigurationProperties,
        RetryRegistry retryRegistry,
			/* @Autowired(required = false) List<RetryAspectExt> retryAspectExtList, */
        FallbackDecorators fallbackDecorators,
			/* SpelResolver spelResolver, */
        ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor
    ) {
    	System.out.println("Calling TSBRetryAOP Injection!!!!");
		return new TSBRetryAop(retryConfigurationProperties,
				retryRegistry, /* retryAspectExtList, */
				fallbackDecorators, /* spelResolver, */ contextAwareScheduledThreadPoolExecutor);
    }

    
    /*@Bean
    @Conditional(value = {RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public RxJava2RetryAspectExt rxJava2RetryAspectExt() {
        return new RxJava2RetryAspectExt();
    }

    @Bean
    @Conditional(value = {ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorRetryAspectExt reactorRetryAspectExt() {
        return new ReactorRetryAspectExt();
    }*/
    
    @Bean
    public EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
}
