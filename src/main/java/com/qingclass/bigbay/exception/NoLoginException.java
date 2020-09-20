package com.qingclass.bigbay.exception;

import java.util.Formatter;

@SuppressWarnings("serial")
public class NoLoginException extends RuntimeException {
	
	public NoLoginException(String message) {
	    super(message);
	}

	@SuppressWarnings("resource")
	public NoLoginException(String format, Object... args) {
	    super(new Formatter().format(format, args).toString());
	}

}
