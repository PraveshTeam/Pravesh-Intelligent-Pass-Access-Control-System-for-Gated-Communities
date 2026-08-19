package com.pravesh.exception;

public class InvalidCredentialsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5438047584307537066L;

	public InvalidCredentialsException(String message) {
        super(message);
    }
}