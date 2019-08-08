package com.example.handlers;

import io.undertow.server.HttpHandler;

public class ExceptionHandler extends io.undertow.server.handlers.ExceptionHandler {

	public ExceptionHandler(HttpHandler handler) {
		super(handler);
	}	
}
