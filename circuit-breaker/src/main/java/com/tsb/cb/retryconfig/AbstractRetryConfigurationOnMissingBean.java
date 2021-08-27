package com.tsb.cb.retryconfig;

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

import com.tsb.cb.aop.TSBRetryAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;

import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;

@Configuration
@Import({ FallbackConfigurationOnMissingBean.class/* , SpelResolverConfigurationOnMissingBean.class */})
public abstract class AbstractRetryConfigurationOnMissingBean {

	@Autowired
	protected final RetryConfiguration retryConfiguration;

    public AbstractRetryConfigurationOnMissingBean() {
        this.retryConfiguration = new RetryConfiguration();
    }

    @Bean
    @Qualifier("compositeRetryCustomizer")
    @ConditionalOnMissingBean(name = "compositeRetryCustomizer")
    public CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer(
         List<RetryConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }
    
    @Bean
    public RetryConfigCustomizer testRetryConfigCustomizer() {
    	return RetryConfigCustomizer.of("backendA", builder -> builder.build());
    }

    /**
     * @param retryConfigurationProperties retryConfigurationProperties retry configuration spring
     *                                     properties
     * @param retryEventConsumerRegistry   the event retry registry
     * @return the retry definition registry
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RetryConfigurationProperties retryConfigurationProperties,
        EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry,
        RegistryEventConsumer<Retry> retryRegistryEventConsumer,
			/* @Qualifier("compositeRetryCustomizer") */CompositeCustomizer<RetryConfigCustomizer> compositeRetryCustomizer) {
        return retryConfiguration
            .retryRegistry(retryConfigurationProperties, retryEventConsumerRegistry,
                retryRegistryEventConsumer, compositeRetryCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<Retry>>> optionalRegistryEventConsumers) {
        return retryConfiguration.retryRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    /**
     * @param retryConfigurationProperties retry configuration spring properties
     * @param retryRegistry                retry in memory registry
     * @return the spring retry AOP aspect
     */
    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public TSBRetryAop retryAspect(
        RetryConfigProperties retryConfigurationProperties,
        RetryRegistry retryRegistry,
			/* @Autowired(required = false) List<RetryAspectExt> retryAspectExtList, */
        FallbackDecorators fallbackDecorators,
			/* SpelResolver spelResolver, */
        @Qualifier("contextAwareScheduledThreadPoolExecutor") ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor
    ) {
        return retryConfiguration
				.retryAspect(retryConfigurationProperties,
						retryRegistry, /* retryAspectExtList, */
						fallbackDecorators, /* spelResolver, */ contextAwareScheduledThreadPoolExecutor);
    }
    
    

    /*@Bean
    @Conditional(value = {RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RxJava2RetryAspectExt rxJava2RetryAspectExt() {
        return retryConfiguration.rxJava2RetryAspectExt();
    }

    @Bean
    @Conditional(value = {ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorRetryAspectExt reactorRetryAspectExt() {
        return retryConfiguration.reactorRetryAspectExt();
    }*/
	
}
