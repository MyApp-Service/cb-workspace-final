package com.tsb.cb.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

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
import com.tsb.cb.annotation.TSBBulkhead;
import com.tsb.cb.bulkheadconfig.BulkheadConfigurationProperties;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.FallbackMethod;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.core.lang.Nullable;
import io.vavr.CheckedFunction0;

@Aspect
public class TSBBulkheadAop implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(TSBBulkheadAop.class);

    private final BulkheadConfigurationProperties bulkheadConfigurationProperties;
    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    /*private final @Nullable
    List<BulkheadAspectExt> bulkheadAspectExts;*/
    private final FallbackDecorators fallbackDecorators;
    //private final SpelResolver spelResolver;

    public TSBBulkheadAop(BulkheadConfigurationProperties backendMonitorPropertiesRegistry,
                          ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry, BulkheadRegistry bulkheadRegistry,
			/* @Autowired(required = false) List<BulkheadAspectExt> bulkheadAspectExts, */
                          FallbackDecorators fallbackDecorators
                          /*SpelResolver spelResolver*/) {
        this.bulkheadConfigurationProperties = backendMonitorPropertiesRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
        //this.bulkheadAspectExts = bulkheadAspectExts;
        this.fallbackDecorators = fallbackDecorators;
        this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
        //this.spelResolver = spelResolver;
    }

    @Pointcut(value = "@within(Bulkhead) || @annotation(Bulkhead)", argNames = "Bulkhead")
    public void matchAnnotatedClassOrMethod(TSBBulkhead Bulkhead) {
    }

    @Around(value = "matchAnnotatedClassOrMethod(bulkheadAnnotation)", argNames = "proceedingJoinPoint, bulkheadAnnotation")
    public Object bulkheadAroundAdvice(ProceedingJoinPoint proceedingJoinPoint,
        @Nullable TSBBulkhead bulkheadAnnotation) throws Throwable {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        if (bulkheadAnnotation == null) {
            bulkheadAnnotation = getBulkheadAnnotation(proceedingJoinPoint);
        }
        if (bulkheadAnnotation == null) { //because annotations wasn't found
            return proceedingJoinPoint.proceed();
        }
        Class<?> returnType = method.getReturnType();
        String backend = bulkheadAnnotation.name();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), bulkheadAnnotation.name());
        String fallbackMethodValue = bulkheadAnnotation.fallback();//spelResolver.resolve(method, proceedingJoinPoint.getArgs(), bulkheadAnnotation.fallbackMethod());
        if (bulkheadAnnotation.type() == TSBBulkhead.Type.THREADPOOL) {
            if (StringUtils.isEmpty(fallbackMethodValue)) {
                return proceedInThreadPoolBulkhead(proceedingJoinPoint, methodName, returnType,
                    backend);
            }
            return executeFallBack(proceedingJoinPoint, fallbackMethodValue, method,
                () -> proceedInThreadPoolBulkhead(proceedingJoinPoint, methodName, returnType,
                    backend));
        } else {
            io.github.resilience4j.bulkhead.Bulkhead bulkhead = getOrCreateBulkhead(methodName,
                backend);
            if (StringUtils.isEmpty(fallbackMethodValue)) {
                return proceed(proceedingJoinPoint, methodName, bulkhead, returnType);
            }
            return executeFallBack(proceedingJoinPoint, fallbackMethodValue, method,
                () -> proceed(proceedingJoinPoint, methodName, bulkhead, returnType));
        }

    }

    private Object executeFallBack(ProceedingJoinPoint proceedingJoinPoint, String fallBackMethod,
        Method method, CheckedFunction0<Object> bulkhead) throws Throwable {
        FallbackMethod fallbackMethod = FallbackMethod
            .create(fallBackMethod, method, proceedingJoinPoint.getArgs(),
                proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod, bulkhead).apply();
    }

    /**
     * entry logic for semaphore bulkhead execution
     *
     * @param proceedingJoinPoint AOP proceedingJoinPoint
     * @param methodName          AOP method name
     * @param bulkhead            the configured bulkhead
     * @param returnType          the AOP method return type
     * @return the result Object of the method call
     * @throws Throwable
     */
    private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
        Bulkhead bulkhead, Class<?> returnType) throws Throwable {
        /*if (bulkheadAspectExts != null && !bulkheadAspectExts.isEmpty()) {
            for (BulkheadAspectExt bulkHeadAspectExt : bulkheadAspectExts) {
                if (bulkHeadAspectExt.canHandleReturnType(returnType)) {
                    return bulkHeadAspectExt.handle(proceedingJoinPoint, bulkhead, methodName);
                }
            }
        }*/
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            return handleJoinPointCompletableFuture(proceedingJoinPoint, bulkhead);
        }
        return handleJoinPoint(proceedingJoinPoint, bulkhead);
    }

    private Bulkhead getOrCreateBulkhead(String methodName,
        String backend) {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(backend);

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Created or retrieved bulkhead '{}' with max concurrent call '{}' and max wait time '{}ms' for method: '{}'",
                backend, bulkhead.getBulkheadConfig().getMaxConcurrentCalls(),
                bulkhead.getBulkheadConfig().getMaxWaitDuration().toMillis(), methodName);
        }

        return bulkhead;
    }

    /**
     * @param proceedingJoinPoint AOP proceedingJoinPoint
     * @return Bulkhead annotation
     */
    @Nullable
    private TSBBulkhead getBulkheadAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("bulkhead parameter is null");
        }
        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
            logger
                .debug("The bulkhead annotation is kept on a interface which is acting as a proxy");
            return AnnotationExtractor
                .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TSBBulkhead.class);
        } else {
            return AnnotationExtractor
                .extract(proceedingJoinPoint.getTarget().getClass(), TSBBulkhead.class);
        }
    }

    /**
     * Sync bulkhead execution
     *
     * @param proceedingJoinPoint AOP proceedingJoinPoint
     * @param bulkhead            the configured bulkhead for that backend call
     * @return the result object
     * @throws Throwable
     */
    private Object handleJoinPoint(ProceedingJoinPoint proceedingJoinPoint,
        io.github.resilience4j.bulkhead.Bulkhead bulkhead) throws Throwable {
        return bulkhead.executeCheckedSupplier(proceedingJoinPoint::proceed);
    }

    /**
     * handle the asynchronous completable future flow
     *
     * @param proceedingJoinPoint AOPJoinPoint
     * @param bulkhead            configured bulkhead
     * @return CompletionStage
     */
    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint proceedingJoinPoint,
        io.github.resilience4j.bulkhead.Bulkhead bulkhead) {
        return bulkhead.executeCompletionStage(() -> {
            try {
                return (CompletionStage<?>) proceedingJoinPoint.proceed();
            } catch (Throwable e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * execute the logic wrapped by ThreadPool bulkhead , please check {@link
     * io.github.resilience4j.bulkhead.ThreadPoolBulkhead} for more information
     *
     * @param proceedingJoinPoint AOP proceedingJoinPoint
     * @param methodName          AOP method name
     * @param returnType          AOP method return type
     * @param backend             backend name
     * @return result Object which will be CompletableFuture instance
     * @throws Throwable
     */
    private Object proceedInThreadPoolBulkhead(ProceedingJoinPoint proceedingJoinPoint,
        String methodName, Class<?> returnType, String backend) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("ThreadPool bulkhead invocation for method {} in backend {}", methodName,
                backend);
        }
        ThreadPoolBulkhead threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead(backend);
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            // threadPoolBulkhead.executeSupplier throws a BulkheadFullException, if the Bulkhead is full.
            // The RuntimeException is converted into an exceptionally completed future
            try {
                return threadPoolBulkhead.executeSupplier(() -> {
                    try {
                        return ((CompletionStage<?>) proceedingJoinPoint.proceed())
                            .toCompletableFuture().get();
                    } catch (ExecutionException e) {
                        throw new CompletionException(e.getCause());
                    } catch (Throwable e) {
                        throw new CompletionException(e);
                    }
                });
            } catch (BulkheadFullException ex){
                CompletableFuture<?> future = new CompletableFuture<>();
                future.completeExceptionally(ex);
                return future;
            }
        } else {
            throw new IllegalStateException(
                "ThreadPool bulkhead is only applicable for completable futures ");
        }
    }


    @Override
    public int getOrder() {
        return bulkheadConfigurationProperties.getBulkheadAspectOrder();
    }
}
