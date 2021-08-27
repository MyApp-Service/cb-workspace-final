package com.tsb.cb.config;

import java.util.function.Predicate;

import org.springframework.stereotype.Component;

@Component
public class TSBExceptionPredicate implements Predicate<Throwable>{

	private static String recordExceptions[];
	
	public void setExceptions(String exceptions) {
		System.out.println(exceptions);
		if(exceptions!=null && exceptions.trim().length()>0) {
			recordExceptions = exceptions.split(",");
		}
		
	}
	
	@Override
	public boolean test(Throwable t) {
		// TODO Auto-generated method stub
		Throwable tCause = t.getCause();
		if(tCause!=null) {
			String tName = tCause.getClass().getName();
			return canRecordException(tName);
		}else {
			String tName = t.getClass().getName();
			return canRecordException(tName);
		}
		
	}
	
	public boolean canRecordException(String tName) {
		for(String exception: recordExceptions) {
			System.out.println(tName+"---"+exception);
			if(tName.equalsIgnoreCase(exception)) {
				return true;
			}
			
		}
		return false;
	}

}
