package com.tsb.cb.retrycore;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.resilience4j.core.lang.Nullable;

public class ContextAwareScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    private final List<ContextPropagator> contextPropagators;
    private static final String THREAD_PREFIX = "ContextAwareScheduledThreadPool";

    @Autowired
    public ContextAwareScheduledThreadPoolExecutor(int corePoolSize,
                                                   @Nullable List<ContextPropagator> contextPropagators) {
        super(corePoolSize, new NamingThreadFactory(THREAD_PREFIX));
        this.contextPropagators = contextPropagators != null ? contextPropagators : new ArrayList<>();
    }

    public List<ContextPropagator> getContextPropagators() {
        return Collections.unmodifiableList(this.contextPropagators);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.schedule(ContextPropagator.decorateRunnable(contextPropagators, () -> {
                try {
                    setMDCContext(mdcContextMap);
                    command.run();
                } finally {
                    MDC.clear();
                }
            }), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.schedule(ContextPropagator.decorateCallable(contextPropagators, () -> {
            try {
                setMDCContext(mdcContextMap);
                return callable.call();
            } finally {
                MDC.clear();
            }
        }), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.scheduleAtFixedRate(ContextPropagator.decorateRunnable(contextPropagators, () -> {
            try {
                setMDCContext(mdcContextMap);
                command.run();
            } finally {
                MDC.clear();
            }
        }), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        Map<String, String> mdcContextMap = getMdcContextMap();
        return super.scheduleWithFixedDelay(ContextPropagator.decorateRunnable(contextPropagators, () -> {
            try {
                setMDCContext(mdcContextMap);
                command.run();
            } finally {
                MDC.clear();
            }
        }), initialDelay, delay, unit);
    }

    private Map<String, String> getMdcContextMap() {
        return Optional.ofNullable(MDC.getCopyOfContextMap()).orElse(Collections.emptyMap());
    }

    private void setMDCContext(Map<String, String> contextMap) {
        MDC.clear();
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }

    public static Builder newScheduledThreadPool() {
        return new Builder();
    }

    public static class Builder {
        private List<ContextPropagator> contextPropagators = new ArrayList<>();
        private int corePoolSize;

        public Builder corePoolSize(int corePoolSize) {
            if (corePoolSize < 1) {
                throw new IllegalArgumentException(
                    "corePoolSize must be a positive integer value >= 1");
            }
            this.corePoolSize = corePoolSize;
            return this;
        }

        public Builder contextPropagators(ContextPropagator... contextPropagators) {
            this.contextPropagators = contextPropagators != null ?
                Arrays.stream(contextPropagators).collect(toList()) :
                new ArrayList<>();
            return this;
        }

        @Bean
        @Primary
        public ContextAwareScheduledThreadPoolExecutor build() {
            return new ContextAwareScheduledThreadPoolExecutor(corePoolSize, contextPropagators);
        }
    }
}
