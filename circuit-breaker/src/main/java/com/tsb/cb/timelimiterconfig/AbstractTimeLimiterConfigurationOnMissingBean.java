package com.tsb.cb.timelimiterconfig;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBTimeLimiterAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retryconfig.RetryConfigCustomizer;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;

import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterEvent;

@Configuration
@Import({ FallbackConfigurationOnMissingBean.class/* , SpelResolverConfigurationOnMissingBean.class */})
public abstract class AbstractTimeLimiterConfigurationOnMissingBean {

	protected final TimeLimiterConfiguration timeLimiterConfiguration;

    protected AbstractTimeLimiterConfigurationOnMissingBean() {
        this.timeLimiterConfiguration = new TimeLimiterConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeTimeLimiterCustomizer")
    @Qualifier("compositeTimeLimiterCustomizer")
    public CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer(
        List<TimeLimiterConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeLimiterRegistry timeLimiterRegistry(
        TimeLimiterConfigurationProperties timeLimiterProperties,
        EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry,
        RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer,
        @Qualifier("compositeTimeLimiterCustomizer") CompositeCustomizer<TimeLimiterConfigCustomizer> compositeTimeLimiterCustomizer) {
        return timeLimiterConfiguration.timeLimiterRegistry(
            timeLimiterProperties, timeLimiterEventsConsumerRegistry,
            timeLimiterRegistryEventConsumer, compositeTimeLimiterCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<TimeLimiter> timeLimiterRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<TimeLimiter>>> optionalRegistryEventConsumers) {
        return timeLimiterConfiguration.timeLimiterRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(AspectJOnClasspathCondition.class)
    @ConditionalOnMissingBean
    public TSBTimeLimiterAop timeLimiterAspect(
        TimeLimiterConfigurationProperties timeLimiterProperties,
        TimeLimiterRegistry timeLimiterRegistry,
        //@Autowired(required = false) List<TimeLimiterAspectExt> timeLimiterAspectExtList,
        FallbackDecorators fallbackDecorators,
        //SpelResolver spelResolver,
        ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor
    ) {
        return timeLimiterConfiguration.timeLimiterAspect(
				timeLimiterProperties,
				timeLimiterRegistry/* , timeLimiterAspectExtList */, fallbackDecorators,
				/* spelResolver, */ contextAwareScheduledThreadPoolExecutor);
    }
    
    @Bean
    public TimeLimiterConfigCustomizer testTimeLimiterConfigCustomizer() {
    	return TimeLimiterConfigCustomizer.of("backendA", builder -> builder.build());
    }

    /*@Bean
    @Conditional({RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RxJava2TimeLimiterAspectExt rxJava2TimeLimiterAspectExt() {
        return timeLimiterConfiguration.rxJava2TimeLimiterAspectExt();
    }

    @Bean
    @Conditional({ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorTimeLimiterAspectExt reactorTimeLimiterAspectExt() {
        return timeLimiterConfiguration.reactorTimeLimiterAspectExt();
    }*/
}
