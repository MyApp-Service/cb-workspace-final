package com.tsb.cb.timelimiterconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBTimeLimiterAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.events.DefaultEventConsumerRegistry;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.timelimiterconfig.TimeLimiterConfigurationProperties.InstanceProperties;

import io.github.resilience4j.core.registry.CompositeRegistryEventConsumer;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterEvent;
import io.vavr.collection.HashMap;

@Configuration
public class TimeLimiterConfiguration {

	@Bean
    @Qualifier("compositeTimeLimiterCustomizer")
    public CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer(
        List<TimeLimiterConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry(
        TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
        EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventConsumerRegistry,
        RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
        @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {
        TimeLimiterRegistry timeLimiterRegistry =
                createTimeLimiterRegistry(timeLimiterConfigurationProperties, timeLimiterRegistryEventConsumer,
                    compositeTimeLimiterCustomizer);
        registerEventConsumer(timeLimiterRegistry, timeLimiterEventConsumerRegistry, timeLimiterConfigurationProperties);

        initTimeLimiterRegistry(timeLimiterRegistry, timeLimiterConfigurationProperties, compositeTimeLimiterCustomizer);
        return timeLimiterRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer(
            Optional<List<RegistryEventConsumer<TimeLimiter>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    @Bean
    @Conditional(AspectJOnClasspathCondition.class)
    public TSBTimeLimiterAop timeLimiterAspect(
        TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
        TimeLimiterRegistry timeLimiterRegistry,
        //@Autowired(required = false) List<TimeLimiterAspectExt> timeLimiterAspectExtList,
        FallbackDecorators fallbackDecorators,
			/* SpelResolver spelResolver, */
        ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor
    ) {
		return new TSBTimeLimiterAop(timeLimiterRegistry, timeLimiterConfigurationProperties,
				/* timeLimiterAspectExtList, */ fallbackDecorators,
				/* spelResolver, */ contextAwareScheduledThreadPoolExecutor);
    }

    /*@Bean
    @Conditional({RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public RxJava2TimeLimiterAspectExt rxJava2TimeLimiterAspectExt() {
        return new RxJava2TimeLimiterAspectExt();
    }

    @Bean
    @Conditional({ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorTimeLimiterAspectExt reactorTimeLimiterAspectExt() {
        return new ReactorTimeLimiterAspectExt();
    }*/

    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances.
     * The EventConsumerRegistry is used by the TimeLimiter events monitor to show the latest TimeLimiter events
     * for each TimeLimiter instance.
     *
     * @return a default EventConsumerRegistry {@link DefaultEventConsumerRegistry}
     */
    @Bean
    public EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
    /**
     * Initializes a timeLimiter registry.
     *
     * @param timeLimiterConfigurationProperties The timeLimiter configuration properties.
     * @return a timeLimiterRegistry
     */
    private static TimeLimiterRegistry createTimeLimiterRegistry(
        TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
        RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
        CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {

        Map<String, TimeLimiterConfig> configs = timeLimiterConfigurationProperties.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> timeLimiterConfigurationProperties.createTimeLimiterConfig(
                            entry.getKey(), entry.getValue(), compositeTimeLimiterCustomizer)));

        return TimeLimiterRegistry.of(configs, timeLimiterRegistryEventConsumer,
            HashMap.ofAll(timeLimiterConfigurationProperties.getTags()));
    }

    /**
     * Initializes the TimeLimiter registry.
     *
     * @param timeLimiterRegistry The time limiter registry.
     * @param compositeTimeLimiterCustomizer The Composite time limiter customizer
     */
    void initTimeLimiterRegistry(
        TimeLimiterRegistry timeLimiterRegistry,
        TimeLimiterConfigurationProperties timeLimiterConfigurationProperties,
        CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {

        timeLimiterConfigurationProperties.getInstances().forEach(
            (name, properties) -> timeLimiterRegistry.timeLimiter(name,
                timeLimiterConfigurationProperties
                    .createTimeLimiterConfig(name, properties, compositeTimeLimiterCustomizer))
        );
    }
    /**
     * Registers the post creation consumer function that registers the consumer events to the timeLimiters.
     *
     * @param timeLimiterRegistry   The timeLimiter registry.
     * @param eventConsumerRegistry The event consumer registry.
     * @param properties timeLimiter configuration properties
     */
    private static void registerEventConsumer(TimeLimiterRegistry timeLimiterRegistry,
                                              EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry,
                                              TimeLimiterConfigurationProperties properties) {
        timeLimiterRegistry.getEventPublisher()
            .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
            .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private static void registerEventConsumer(EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry, TimeLimiter timeLimiter,
                                              TimeLimiterConfigurationProperties timeLimiterConfigurationProperties) {
        int eventConsumerBufferSize = Optional.ofNullable(timeLimiterConfigurationProperties.getInstanceProperties(timeLimiter.getName()))
                .map(InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);
        timeLimiter.getEventPublisher().onEvent(
            eventConsumerRegistry.createEventConsumer(timeLimiter.getName(), eventConsumerBufferSize));
    }
	
}
