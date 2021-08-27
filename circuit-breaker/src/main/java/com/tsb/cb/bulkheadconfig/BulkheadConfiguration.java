package com.tsb.cb.bulkheadconfig;

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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBBulkheadAop;
import com.tsb.cb.bulkheadconfig.BulkheadConfigurationProperties.InstanceProperties;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.config.FallbackConfiguration;
import com.tsb.cb.events.DefaultEventConsumerRegistry;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.service.FallbackDecorators;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.core.registry.CompositeRegistryEventConsumer;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

@Configuration
@Import({ ThreadPoolBulkheadConfiguration.class, FallbackConfiguration.class/* , SpelResolverConfiguration.class */})
public class BulkheadConfiguration {

	@Bean
    @Qualifier("compositeBulkheadCustomizer")
    public CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer(
        List<BulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    /**
     * @param bulkheadConfigurationProperties bulk head spring configuration properties
     * @param bulkheadEventConsumerRegistry   the bulk head event consumer registry
     * @return the BulkheadRegistry with all needed setup in place
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry(
        BulkheadConfigurationProperties bulkheadConfigurationProperties,
        EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
        RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
			/* @Qualifier("compositeBulkheadCustomizer") */ CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
        BulkheadRegistry bulkheadRegistry = createBulkheadRegistry(bulkheadConfigurationProperties,
            bulkheadRegistryEventConsumer, compositeBulkheadCustomizer);
        registerEventConsumer(bulkheadRegistry, bulkheadEventConsumerRegistry,
            bulkheadConfigurationProperties);
        bulkheadConfigurationProperties.getInstances().forEach((name, properties) ->
            bulkheadRegistry
                .bulkhead(name, bulkheadConfigurationProperties
                    .createBulkheadConfig(properties, compositeBulkheadCustomizer,
                        name)));
        return bulkheadRegistry;
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<Bulkhead>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(
            optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }

    /**
     * Initializes a bulkhead registry.
     *
     * @param bulkheadConfigurationProperties The bulkhead configuration properties.
     * @param compositeBulkheadCustomizer
     * @return a BulkheadRegistry
     */
    private BulkheadRegistry createBulkheadRegistry(
            BulkheadConfigurationProperties bulkheadConfigurationProperties,
            RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
            CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
            Map<String, BulkheadConfig> configs = bulkheadConfigurationProperties.getConfigs()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> bulkheadConfigurationProperties.createBulkheadConfig(entry.getValue(),
                        compositeBulkheadCustomizer, entry.getKey())));
            return BulkheadRegistry.of(configs, bulkheadRegistryEventConsumer,
                io.vavr.collection.HashMap.ofAll(bulkheadConfigurationProperties.getTags()));
        }

    /**
     * Registers the post creation consumer function that registers the consumer events to the
     * bulkheads.
     *
     * @param bulkheadRegistry      The BulkHead registry.
     * @param eventConsumerRegistry The event consumer registry.
     */
    private void registerEventConsumer(BulkheadRegistry bulkheadRegistry,
        EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
        BulkheadConfigurationProperties properties) {
        bulkheadRegistry.getEventPublisher()
            .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry(), properties))
            .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry(), properties));
    }

    private void registerEventConsumer(EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry,
        Bulkhead bulkHead, BulkheadConfigurationProperties bulkheadConfigurationProperties) {
        int eventConsumerBufferSize = Optional
            .ofNullable(bulkheadConfigurationProperties.getBackendProperties(bulkHead.getName()))
            .map(InstanceProperties::getEventConsumerBufferSize)
            .orElse(100);
        bulkHead.getEventPublisher().onEvent(
            eventConsumerRegistry.createEventConsumer(bulkHead.getName(), eventConsumerBufferSize));
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    public TSBBulkheadAop bulkheadAspect(
        BulkheadConfigurationProperties bulkheadConfigurationProperties,
        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
        BulkheadRegistry bulkheadRegistry,
        //@Autowired(required = false) List<BulkheadAspectExt> bulkHeadAspectExtList,
        FallbackDecorators fallbackDecorators
        //SpelResolver spelResolver
    ) {
        return new TSBBulkheadAop(bulkheadConfigurationProperties, threadPoolBulkheadRegistry,
				bulkheadRegistry/* , bulkHeadAspectExtList */, fallbackDecorators/* , spelResolver */);
    }

    /*@Bean
    @Conditional(value = {RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public RxJava2BulkheadAspectExt rxJava2BulkHeadAspectExt() {
        return new RxJava2BulkheadAspectExt();
    }

    @Bean
    @Conditional(value = {ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    public ReactorBulkheadAspectExt reactorBulkHeadAspectExt() {
        return new ReactorBulkheadAspectExt();
    }*/

    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances. The
     * EventConsumerRegistry is used by the BulkheadHealthIndicator to show the latest Bulkhead
     * events for each Bulkhead instance.
     *
     * @return a default EventConsumerRegistry {@link DefaultEventConsumerRegistry}
     */
    @Bean
    public EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
	
}
