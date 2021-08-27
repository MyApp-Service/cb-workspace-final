package com.tsb.cb.retryconfig;

import org.springframework.core.Ordered;

public class RetryConfigProperties extends RetryConfigurationProperties{

	
	 private int retryAspectOrder = Ordered.LOWEST_PRECEDENCE - 4;

	    /**
	     * As of release 0.16.0 as we set an implicit spring aspect order now which is retry then
	     * circuit breaker then rate limiter then bulkhead but the user can override it still if he has
	     * different use case but bulkhead will be first aspect all the time due to the implicit order
	     * we have it for bulkhead
	     */
	    public int getRetryAspectOrder() {
	        return retryAspectOrder;
	    }

	    /**
	     * set retry aspect order
	     *
	     * @param retryAspectOrder retry aspect target order
	     */
	    public void setRetryAspectOrder(int retryAspectOrder) {
	        this.retryAspectOrder = retryAspectOrder;
	    }
}
