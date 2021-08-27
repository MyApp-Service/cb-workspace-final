package com.tsb.cb.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.tsb.cb.annotation.TSBTimeLimiter;
import com.tsb.cb.exception.IllegalReturnTypeException;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.FallbackMethod;
import com.tsb.cb.timelimiterconfig.TimeLimiterConfigurationProperties;

import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

@Aspect
public class TSBTimeLimiterAop implements Ordered, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TSBTimeLimiterAop.class);

    private final TimeLimiterRegistry timeLimiterRegistry;
    private final TimeLimiterConfigurationProperties properties;
    private final ScheduledExecutorService timeLimiterExecutorService;
	/*
	 * @Nullable private final List<TimeLimiterAspectExt> timeLimiterAspectExtList;
	 */
    private final FallbackDecorators fallbackDecorators;
    //private final SpelResolver spelResolver;

    public TSBTimeLimiterAop(TimeLimiterRegistry timeLimiterRegistry,
                             TimeLimiterConfigurationProperties properties,
                             /*@Nullable List<TimeLimiterAspectExt> timeLimiterAspectExtList,*/
                             FallbackDecorators fallbackDecorators,
                             /*SpelResolver spelResolver,*/
                             @Nullable ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor) {
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.properties = properties;
        //this.timeLimiterAspectExtList = timeLimiterAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        //this.spelResolver = spelResolver;
        this.timeLimiterExecutorService = contextAwareScheduledThreadPoolExecutor != null ?
            contextAwareScheduledThreadPoolExecutor :
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Pointcut(value = "@within(timeLimiter) || @annotation(timeLimiter)", argNames = "timeLimiter")
    public void matchAnnotatedClassOrMethod(TSBTimeLimiter timeLimiter) {
        // a marker method
    }

    @Around(value = "matchAnnotatedClassOrMethod(timeLimiterAnnotation)", argNames = "proceedingJoinPoint, timeLimiterAnnotation")
    public Object timeLimiterAroundAdvice(ProceedingJoinPoint proceedingJoinPoint,
        @Nullable TSBTimeLimiter timeLimiterAnnotation) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if (timeLimiterAnnotation == null) {
            timeLimiterAnnotation = getTimeLimiterAnnotation(proceedingJoinPoint);
        }
        if(timeLimiterAnnotation == null) {
            return proceedingJoinPoint.proceed();
        }
        String name = timeLimiterAnnotation.name();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), timeLimiterAnnotation.name());
        TimeLimiter timeLimiter =
            getOrCreateTimeLimiter(methodName, name);
        Class<?> returnType = method.getReturnType();

        System.out.println("TimeoutDuration:---"+timeLimiter.getTimeLimiterConfig().getTimeoutDuration());
        String fallbackMethodValue = timeLimiterAnnotation.fallback();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), timeLimiterAnnotation.fallbackMethod());
        if (StringUtils.isEmpty(fallbackMethodValue)) {
            return proceed(proceedingJoinPoint, methodName, timeLimiter, returnType);
        }
        FallbackMethod fallbackMethod = FallbackMethod
            .create(fallbackMethodValue, method,
                proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod,
            () -> proceed(proceedingJoinPoint, methodName, timeLimiter, returnType)).apply();
    }

    private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
        TimeLimiter timeLimiter, Class<?> returnType)
        throws Throwable {
        /*if (timeLimiterAspectExtList != null && !timeLimiterAspectExtList.isEmpty()) {
            for (TimeLimiterAspectExt timeLimiterAspectExt : timeLimiterAspectExtList) {
                if (timeLimiterAspectExt.canHandleReturnType(returnType)) {
                    return timeLimiterAspectExt.handle(proceedingJoinPoint, timeLimiter, methodName);
                }
            }
        }*/

    	System.out.println("returnType from AOP:---"+returnType+"==="+CompletionStage.class.isAssignableFrom(returnType));
        if (!CompletionStage.class.isAssignableFrom(returnType)) {
			/*
			 * throw new IllegalReturnTypeException(returnType, methodName,
			 * "CompletionStage expected.");
			 */
        	return handleJoinPoint(proceedingJoinPoint,timeLimiter);
        }

        return handleJoinPointCompletableFuture(proceedingJoinPoint, timeLimiter);
    	/*if (CompletionStage.class.isAssignableFrom(returnType)) {
            return handleJoinPointCompletableFuture(proceedingJoinPoint, timeLimiter);
        }
        return handleJoinPoint(proceedingJoinPoint, timeLimiter);*/
    }
    
    private Object handleJoinPoint(ProceedingJoinPoint proceedingJoinPoint,
    		TimeLimiter timeLimiter) throws Throwable {
    	return timeLimiter.executeFutureSupplier(
    			  () -> CompletableFuture.supplyAsync(() -> {
					/*
					 * try { return proceedJointPointObj(proceedingJoinPoint); } catch (Throwable e)
					 * { // TODO Auto-generated catch block e.printStackTrace(); }
					 */
					try {
						return proceedingJoinPoint.proceed();
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return null;
				}));
            //return timeLimiter.executeFutureSupplier(()->CompletableFuture.supplyAsync(()->proceedingJoinPoint.proceed()));
        }
    
    private static Object proceedJointPointObj(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    	return proceedingJoinPoint.proceed();
    }

    private TimeLimiter getOrCreateTimeLimiter(String methodName, String name) {
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(name);

        if (logger.isDebugEnabled()) {
            TimeLimiterConfig timeLimiterConfig = timeLimiter.getTimeLimiterConfig();
            logger.debug(
                    "Created or retrieved time limiter '{}' with timeout duration '{}' and cancelRunningFuture '{}' for method: '{}'",
                    name, timeLimiterConfig.getTimeoutDuration(), timeLimiterConfig.shouldCancelRunningFuture(), methodName
            );
        }

        return timeLimiter;
    }

    @Nullable
    private static TSBTimeLimiter getTimeLimiterAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug("The TimeLimiter annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor.extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TSBTimeLimiter.class);
        } else {
            return AnnotationExtractor.extract(proceedingJoinPoint.getTarget().getClass(), TSBTimeLimiter.class);
        }
    }

    private Object handleJoinPointCompletableFuture(
            ProceedingJoinPoint proceedingJoinPoint, TimeLimiter timeLimiter) throws Throwable {
        return timeLimiter.executeCompletionStage(timeLimiterExecutorService, () -> {
            try {
                return (CompletionStage<?>) proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    @Override
    public int getOrder() {
        return properties.getTimeLimiterAspectOrder();
    }

    @Override
    public void close() throws Exception {
        timeLimiterExecutorService.shutdown();
        try {
            if (!timeLimiterExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                timeLimiterExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if (!timeLimiterExecutorService.isTerminated()) {
                timeLimiterExecutorService.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
    }
}

