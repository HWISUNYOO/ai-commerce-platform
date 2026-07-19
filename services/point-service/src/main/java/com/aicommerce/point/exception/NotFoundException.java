package com.aicommerce.point.exception;

public class NotFoundException extends RuntimeException {

	public NotFoundException(String resource, Object id) {
		super(resource + " not found: " + id);
	}
}
