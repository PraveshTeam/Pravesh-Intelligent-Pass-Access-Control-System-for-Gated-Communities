package com.pravesh.exception;

public class ShiftRequiredException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8451173248936831246L;

	public ShiftRequiredException(String message) {
        super(message);
    }
}