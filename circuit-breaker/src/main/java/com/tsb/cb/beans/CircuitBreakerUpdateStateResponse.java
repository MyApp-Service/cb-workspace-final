package com.tsb.cb.beans;

import java.util.Objects;

public class CircuitBreakerUpdateStateResponse {

		private String circuitBreakerName;
	    private String currentState;
	    private String message;

	    public String getCircuitBreakerName() {
	        return circuitBreakerName;
	    }

	    public void setCircuitBreakerName(String circuitBreakerName) {
	        this.circuitBreakerName = circuitBreakerName;
	    }

	    public String getCurrentState() {
	        return currentState;
	    }

	    public void setCurrentState(String currentState) {
	        this.currentState = currentState;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public void setMessage(String message) {
	        this.message = message;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        CircuitBreakerUpdateStateResponse that = (CircuitBreakerUpdateStateResponse) o;
	        return circuitBreakerName.equals(that.circuitBreakerName) &&
	            currentState.equals(that.currentState) &&
	            message.equals(that.message);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(circuitBreakerName, currentState, message);
	    }

	    @Override
	    public String toString() {
	        return "CircuitBreakerUpdateStateResponse{" +
	            "circuitBreakerName='" + circuitBreakerName + '\'' +
	            ", currentState='" + currentState + '\'' +
	            ", message='" + message + '\'' +
	            '}';
	    }
}
