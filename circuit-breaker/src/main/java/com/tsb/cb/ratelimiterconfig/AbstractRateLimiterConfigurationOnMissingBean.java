package com.tsb.cb.ratelimiterconfig;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBRateLimiterAop;
import com.tsb.cb.config.AspectJOnClasspathCondition;
import com.tsb.cb.config.CompositeCustomizer;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.timelimiterconfig.TimeLimiterConfigCustomizer;

import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.event.RateLimiterEvent;

@Configuration
@Import({ FallbackConfigurationOnMissingBean.class/* , SpelResolverConfigurationOnMissingBean.class */})
public abstract class AbstractRateLimiterConfigurationOnMissingBean {

	protected final RateLimiterConfiguration rateLimiterConfiguration;

    public AbstractRateLimiterConfigurationOnMissingBean() {
        this.rateLimiterConfiguration = new RateLimiterConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean(name = "compositeRateLimiterCustomizer")
    @Qualifier("compositeRateLimiterCustomizer")
    public CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer(
        List<RateLimiterConfigCustomizer> configCustomizers) {
        return new CompositeCustomizer<>(configCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(
        RateLimiterConfigurationProperties rateLimiterProperties,
        EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry,
        RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer,
        @Qualifier("compositeRateLimiterCustomizer") CompositeCustomizer<RateLimiterConfigCustomizer> compositeRateLimiterCustomizer) {
        return rateLimiterConfiguration
            .rateLimiterRegistry(rateLimiterProperties, rateLimiterEventsConsumerRegistry,
                rateLimiterRegistryEventConsumer, compositeRateLimiterCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<RateLimiter> rateLimiterRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<RateLimiter>>> optionalRegistryEventConsumers) {
        return rateLimiterConfiguration
            .rateLimiterRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public TSBRateLimiterAop rateLimiterAspect(
        RateLimiterConfigurationProperties rateLimiterProperties,
        RateLimiterRegistry rateLimiterRegistry,
        //@Autowired(required = false) List<RateLimiterAspectExt> rateLimiterAspectExtList,
        FallbackDecorators fallbackDecorators
        //SpelResolver spelResolver
    ) {
        return rateLimiterConfiguration
				.rateLimiterAspect(rateLimiterProperties,
						rateLimiterRegistry, /* rateLimiterAspectExtList, */
						fallbackDecorators/* , spelResolver */);
    }

    @Bean
    public RateLimiterConfigCustomizer testRateLimiterConfigCustomizer() {
    	return RateLimiterConfigCustomizer.of("backendA", builder -> builder.build());
    }
    /*@Bean
    @Conditional(value = {RxJava2OnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public RxJava2RateLimiterAspectExt rxJava2RateLimiterAspectExt() {
        return rateLimiterConfiguration.rxJava2RateLimiterAspectExt();
    }

    @Bean
    @Conditional(value = {ReactorOnClasspathCondition.class, AspectJOnClasspathCondition.class})
    @ConditionalOnMissingBean
    public ReactorRateLimiterAspectExt reactorRateLimiterAspectExt() {
        return rateLimiterConfiguration.reactorRateLimiterAspectExt();
    }*/
}
