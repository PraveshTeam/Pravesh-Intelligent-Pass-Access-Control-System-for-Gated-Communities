package com.pravesh.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

	private boolean success = false;
	private String message;
	private Map<String, String> errors;
	private String path;
	private LocalDateTime timestamp = LocalDateTime.now();

	public ApiErrorResponse(String message, String path) {
		this.message = message;
		this.path = path;
	}

	public ApiErrorResponse(String message, Map<String, String> errors, String path) {
		this.message = message;
		this.errors = errors;
		this.path = path;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public String getPath() {
		return path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}