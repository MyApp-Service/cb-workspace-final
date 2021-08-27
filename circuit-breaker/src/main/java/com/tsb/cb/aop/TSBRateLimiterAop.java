package com.tsb.cb.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import com.tsb.cb.annotation.AnnotationExtractor;
import com.tsb.cb.annotation.TSBRateLimiter;
import com.tsb.cb.ratelimiterconfig.RateLimiterConfigurationProperties;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.FallbackMethod;

import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Aspect
public class TSBRateLimiterAop implements Ordered {

    private static final String RATE_LIMITER_RECEIVED = "Created or retrieved rate limiter '{}' with period: '{}'; limit for period: '{}'; timeout: '{}'; method: '{}'";
    private static final Logger logger = LoggerFactory.getLogger(TSBRateLimiterAop.class);
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfigurationProperties properties;
	/*
	 * private final @Nullable List<RateLimiterAspectExt> rateLimiterAspectExtList;
	 */
    private final FallbackDecorators fallbackDecorators;
    //private final SpelResolver spelResolver;

    public TSBRateLimiterAop(RateLimiterRegistry rateLimiterRegistry,
                             RateLimiterConfigurationProperties properties,
                             //@Autowired(required = false) List<RateLimiterAspectExt> rateLimiterAspectExtList,
                             FallbackDecorators fallbackDecorators
                             /*SpelResolver spelResolver*/) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.properties = properties;
        //this.rateLimiterAspectExtList = rateLimiterAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        //this.spelResolver = spelResolver;
    }

    /**
     * Method used as pointcut
     *
     * @param rateLimiter - matched annotation
     */
    @Pointcut(value = "@within(rateLimiter) || @annotation(rateLimiter)", argNames = "rateLimiter")
    public void matchAnnotatedClassOrMethod(TSBRateLimiter rateLimiter) {
        // Method used as pointcut
    }

    @Around(value = "matchAnnotatedClassOrMethod(rateLimiterAnnotation)", argNames = "proceedingJoinPoint, rateLimiterAnnotation")
    public Object rateLimiterAroundAdvice(ProceedingJoinPoint proceedingJoinPoint,
        @Nullable TSBRateLimiter rateLimiterAnnotation) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if (rateLimiterAnnotation == null) {
            rateLimiterAnnotation = getRateLimiterAnnotation(proceedingJoinPoint);
        }
        if (rateLimiterAnnotation == null) { //because annotations wasn't found
            return proceedingJoinPoint.proceed();
        }
        String name = rateLimiterAnnotation.name();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), rateLimiterAnnotation.name());
        RateLimiter rateLimiter = getOrCreateRateLimiter(
            methodName, name);
        Class<?> returnType = method.getReturnType();

        String fallbackMethodValue = rateLimiterAnnotation.fallback();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), rateLimiterAnnotation.fallbackMethod());
        if (StringUtils.isEmpty(fallbackMethodValue)) {
            return proceed(proceedingJoinPoint, methodName, returnType, rateLimiter);
        }
        FallbackMethod fallbackMethod = FallbackMethod
            .create(fallbackMethodValue, method, proceedingJoinPoint.getArgs(),
                proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod,
            () -> proceed(proceedingJoinPoint, methodName, returnType, rateLimiter)).apply();
    }

    private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
        Class<?> returnType, io.github.resilience4j.ratelimiter.RateLimiter rateLimiter)
        throws Throwable {
        /*if (rateLimiterAspectExtList != null && !rateLimiterAspectExtList.isEmpty()) {
            for (RateLimiterAspectExt rateLimiterAspectExt : rateLimiterAspectExtList) {
                if (rateLimiterAspectExt.canHandleReturnType(returnType)) {
                    return rateLimiterAspectExt
                        .handle(proceedingJoinPoint, rateLimiter, methodName);
                }
            }
        }*/
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            return handleJoinPointCompletableFuture(proceedingJoinPoint, rateLimiter);
        }
        return handleJoinPoint(proceedingJoinPoint, rateLimiter);
    }

    private io.github.resilience4j.ratelimiter.RateLimiter getOrCreateRateLimiter(String methodName,
        String name) {
        io.github.resilience4j.ratelimiter.RateLimiter rateLimiter = rateLimiterRegistry
            .rateLimiter(name);

        if (logger.isDebugEnabled()) {
            RateLimiterConfig rateLimiterConfig = rateLimiter.getRateLimiterConfig();
            logger.debug(
                RATE_LIMITER_RECEIVED,
                name, rateLimiterConfig.getLimitRefreshPeriod(),
                rateLimiterConfig.getLimitForPeriod(),
                rateLimiterConfig.getTimeoutDuration(), methodName
            );
        }

        return rateLimiter;
    }

    @Nullable
    private TSBRateLimiter getRateLimiterAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug(
                "The rate limiter annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor
                .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TSBRateLimiter.class);
        } else {
            return AnnotationExtractor
                .extract(proceedingJoinPoint.getTarget().getClass(), TSBRateLimiter.class);
        }
    }

    private Object handleJoinPoint(ProceedingJoinPoint proceedingJoinPoint,
        io.github.resilience4j.ratelimiter.RateLimiter rateLimiter)
        throws Throwable {
        return rateLimiter.executeCheckedSupplier(proceedingJoinPoint::proceed);
    }

    /**
     * handle the asynchronous completable future flow
     *
     * @param proceedingJoinPoint AOPJoinPoint
     * @param rateLimiter         configured rate limiter
     * @return CompletionStage
     */
    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint proceedingJoinPoint,
        io.github.resilience4j.ratelimiter.RateLimiter rateLimiter) {
        return rateLimiter.executeCompletionStage(() -> {
            try {
                return (CompletionStage<?>) proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }


    @Override
    public int getOrder() {
        return properties.getRateLimiterAspectOrder();
    }
}

