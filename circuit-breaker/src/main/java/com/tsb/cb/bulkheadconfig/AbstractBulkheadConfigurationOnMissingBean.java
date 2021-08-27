package com.tsb.cb.bulkheadconfig;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBBulkheadAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retryconfig.RetryConfigCustomizer;
import com.tsb.cb.service.FallbackDecorators;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

@Configuration
@Import({ FallbackConfigurationOnMissingBean.class/* , SpelResolverConfigurationOnMissingBean.class */})
public abstract class AbstractBulkheadConfigurationOnMissingBean {

	@Autowired
    protected final BulkheadConfiguration bulkheadConfiguration;
	@Autowired
    protected final ThreadPoolBulkheadConfiguration threadPoolBulkheadConfiguration;

    public AbstractBulkheadConfigurationOnMissingBean() {
        this.threadPoolBulkheadConfiguration = new ThreadPoolBulkheadConfiguration();
        this.bulkheadConfiguration = new BulkheadConfiguration();
    }


    @Bean
    @ConditionalOnMissingBean(name = "compositeBulkheadCustomizer")
    @Qualifier("compositeBulkheadCustomizer")
    public CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer(
        List<BulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(
        BulkheadConfigurationProperties bulkheadConfigurationProperties,
        EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
        RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer,
        @Qualifier("compositeBulkheadCustomizer") CompositeCustomizer<BulkheadConfigCustomizer> compositeBulkheadCustomizer) {
        return bulkheadConfiguration
            .bulkheadRegistry(bulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
                bulkheadRegistryEventConsumer, compositeBulkheadCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Bulkhead> bulkheadRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<Bulkhead>>> optionalRegistryEventConsumers) {
        return bulkheadConfiguration.bulkheadRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public TSBBulkheadAop bulkheadAspect(
        BulkheadConfigurationProperties bulkheadConfigurationProperties,
        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
        BulkheadRegistry bulkheadRegistry,
        //@Autowired(required = false) List<BulkheadAspectExt> bulkHeadAspectExtList,
        FallbackDecorators fallbackDecorators
        /*SpelResolver spelResolver*/) {
        return bulkheadConfiguration
            .bulkheadAspect(bulkheadConfigurationProperties, threadPoolBulkheadRegistry,
						bulkheadRegistry, /* bulkHeadAspectExtList, */ fallbackDecorators/* , spelResolver */);
    }
    
    @Bean
    public BulkheadConfigCustomizer testBulkheadConfigCustomizer() {
    	return BulkheadConfigCustomizer.of("backendA", builder -> builder.build());
    }
    
    @Bean
    public ThreadPoolBulkheadConfigCustomizer testThreadPoolBulkheadConfigCustomizer() {
    	return ThreadPoolBulkheadConfigCustomizer.of("backendA", builder -> builder.build());
    }

    /*@Bean
    @Conditional(value = {RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RxJava2BulkheadAspectExt rxJava2BulkHeadAspectExt() {
        return bulkheadConfiguration.rxJava2BulkHeadAspectExt();
    }*/

    /*@Bean
    @Conditional(value = {ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorBulkheadAspectExt reactorBulkHeadAspectExt() {
        return bulkheadConfiguration.reactorBulkHeadAspectExt();
    }*/


    @Bean
    @ConditionalOnMissingBean(name = "compositeThreadPoolBulkheadCustomizer")
    @Qualifier("compositeThreadPoolBulkheadCustomizer")
    public CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer(
        List<ThreadPoolBulkheadConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }


    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry(
        ThreadPoolBulkheadConfigurationProperties threadPoolBulkheadConfigurationProperties,
        EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry,
        RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer,
        @Qualifier("compositeThreadPoolBulkheadCustomizer") CompositeCustomizer<ThreadPoolBulkheadConfigCustomizer> compositeThreadPoolBulkheadCustomizer) {
        return threadPoolBulkheadConfiguration.threadPoolBulkheadRegistry(
            threadPoolBulkheadConfigurationProperties, bulkheadEventConsumerRegistry,
            threadPoolBulkheadRegistryEventConsumer, compositeThreadPoolBulkheadCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<ThreadPoolBulkhead> threadPoolBulkheadRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<ThreadPoolBulkhead>>> optionalRegistryEventConsumers) {
        return threadPoolBulkheadConfiguration
            .threadPoolBulkheadRegistryEventConsumer(optionalRegistryEventConsumers);
    }

}
