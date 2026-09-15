package com.pravesh.exception;

public class OtpValidationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2915040577146776608L;

	public OtpValidationException(String message) {
        super(message);
    }
}