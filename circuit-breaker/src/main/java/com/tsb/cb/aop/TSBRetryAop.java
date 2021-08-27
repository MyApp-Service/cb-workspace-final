package com.tsb.cb.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import com.tsb.cb.annotation.TSBRetry;
import com.tsb.cb.retryconfig.RetryConfigProperties;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.FallbackMethod;

import io.github.resilience4j.core.lang.Nullable;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

@Aspect
public class TSBRetryAop  implements Ordered, AutoCloseable{
	
	private static final Logger logger = LoggerFactory.getLogger(TSBRetryAop.class);
	
	private final ScheduledExecutorService retryExecutorService;
    private final RetryConfigProperties retryConfigurationProperties;
    private final RetryRegistry retryRegistry;
    private final FallbackDecorators fallbackDecorators;
    
    public TSBRetryAop(RetryConfigProperties retryConfigurationProperties,
            RetryRegistry retryRegistry,
            //@Autowired(required = false) List<RetryAspectExt> retryAspectExtList,
            FallbackDecorators fallbackDecorators,
            //SpelResolver spelResolver,
            @Nullable ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor
            ) {
		this.retryConfigurationProperties = retryConfigurationProperties;
		this.retryRegistry = retryRegistry;
		//this.retryAspectExtList = retryAspectExtList;
		this.fallbackDecorators = fallbackDecorators;
		//this.spelResolver = spelResolver;
		this.retryExecutorService = contextAwareScheduledThreadPoolExecutor != null ?
		 contextAwareScheduledThreadPoolExecutor :
		 Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		
		System.out.println("TSBRetry Construtor is calling");
	}
    
    @Pointcut(value = "@within(retry) || @annotation(retry)", argNames = "retry")
    public void matchAnnotatedClassOrMethod(TSBRetry retry) {
    	
    	System.out.println("Calling for TSBRetryAOP Functionality...");
    }

    @Around(value = "matchAnnotatedClassOrMethod(retryAnnotation)", argNames = "proceedingJoinPoint, retryAnnotation")
    public Object retryAroundAdvice(ProceedingJoinPoint proceedingJoinPoint,
        @Nullable TSBRetry retryAnnotation) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if (retryAnnotation == null) {
            retryAnnotation = getRetryAnnotation(proceedingJoinPoint);
        }
        if (retryAnnotation == null) { //because annotations wasn't found
            return proceedingJoinPoint.proceed();
        }
        //String backend = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), retryAnnotation.name());
        Retry retry = getOrCreateRetry(methodName, retryAnnotation.name());
        Class<?> returnType = method.getReturnType();
        
        System.out.println("-- Name: --"+retryAnnotation.name());
        System.out.println("-- retry.getRetryConfig().getMaxAttempts() --"+retry.getRetryConfig().getMaxAttempts());
        System.out.println("-- getFailureRateThreshold: --"+retry.getRetryConfig().getIntervalFunction());
        System.out.println("---WaitDuration: ---"+retry.getRetryConfig().DEFAULT_WAIT_DURATION);
        
        //String fallbackMethodValue = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), retryAnnotation.fallbackMethod());
        String fallbackMethodValue = retryAnnotation.fallback();
        if (StringUtils.isEmpty(fallbackMethodValue)) {
            return proceed(proceedingJoinPoint, methodName, retry, returnType);
        }
        FallbackMethod fallbackMethod = FallbackMethod
            .create(fallbackMethodValue, method, proceedingJoinPoint.getArgs(),
                proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod,
            () -> proceed(proceedingJoinPoint, methodName, retry, returnType)).apply();
    }

    private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
            Retry retry, Class<?> returnType) throws Throwable {
            if (CompletionStage.class.isAssignableFrom(returnType)) {
                return handleJoinPointCompletableFuture(proceedingJoinPoint, retry);
            }
            /*if (retryAspectExtList != null && !retryAspectExtList.isEmpty()) {
                for (RetryAspectExt retryAspectExt : retryAspectExtList) {
                    if (retryAspectExt.canHandleReturnType(returnType)) {
                        return retryAspectExt.handle(proceedingJoinPoint, retry, methodName);
                    }
                }
            }*/
            return handleDefaultJoinPoint(proceedingJoinPoint, retry);
        }
    
    /**
     * @param methodName the retry method name
     * @param backend    the retry backend name
     * @return the configured retry
     */
    private Retry getOrCreateRetry(String methodName, String backend) {
        Retry retry = retryRegistry.retry(backend);

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Created or retrieved retry '{}' with max attempts rate '{}'  for method: '{}'",
                backend, retry.getRetryConfig().getMaxAttempts(), methodName);
        }
        return retry;
    }
    
    
    /**
     * @param proceedingJoinPoint the aspect joint point
     * @return the retry annotation
     */
    @Nullable
    private TSBRetry getRetryAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger.debug("The retry annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor
                .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TSBRetry.class);
        } else {
            return AnnotationExtractor
                .extract(proceedingJoinPoint.getTarget().getClass(), TSBRetry.class);
        }
    }
    
    /**
     * @param proceedingJoinPoint the AOP logic joint point
     * @param retry               the configured sync retry
     * @return the result object if any
     * @throws Throwable
     */
    private Object handleDefaultJoinPoint(ProceedingJoinPoint proceedingJoinPoint,
        Retry retry) throws Throwable {
        return retry.executeCheckedSupplier(proceedingJoinPoint::proceed);
    }
    
    /**
     * @param proceedingJoinPoint the AOP logic joint point
     * @param retry               the configured async retry
     * @return the result object if any
     */
    @SuppressWarnings("unchecked")
    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint proceedingJoinPoint,
        Retry retry) {
        return retry.executeCompletionStage(retryExecutorService, () -> {
            try {
                return (CompletionStage<Object>) proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }
    
    @Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		retryExecutorService.shutdown();
        try {
            if (!retryExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if (!retryExecutorService.isTerminated()) {
                retryExecutorService.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
		
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
