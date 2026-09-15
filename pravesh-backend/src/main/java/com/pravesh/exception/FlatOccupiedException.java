package com.pravesh.exception;

import lombok.Getter;

@Getter
public class FlatOccupiedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2921624251373697813L;
	private final Long occupantResidentId;
    private final String occupantName;
    private final String flatNumber;

    public FlatOccupiedException(String message, Long occupantResidentId, String occupantName, String flatNumber) {
        super(message);
        this.occupantResidentId = occupantResidentId;
        this.occupantName = occupantName;
        this.flatNumber = flatNumber;
    }
}