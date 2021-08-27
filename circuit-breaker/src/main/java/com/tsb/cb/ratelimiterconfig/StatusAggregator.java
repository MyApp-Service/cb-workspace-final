package com.tsb.cb.ratelimiterconfig;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.actuate.health.Status;

@FunctionalInterface
public interface StatusAggregator {

	static StatusAggregator getDefault() {
		return SimpleStatusAggregator.INSTANCE;
	}

	/**
	 * Return the aggregate status for the given set of statuses.
	 * @param statuses the statuses to aggregate
	 * @return the aggregate status
	 */
	default Status getAggregateStatus(Status... statuses) {
		return getAggregateStatus(new LinkedHashSet<>(Arrays.asList(statuses)));
	}

	/**
	 * Return the aggregate status for the given set of statuses.
	 * @param statuses the statuses to aggregate
	 * @return the aggregate status
	 */
	Status getAggregateStatus(Set<Status> statuses);
}
