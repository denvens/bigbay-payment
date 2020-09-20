package com.qingclass.bigbay.exception;

import java.util.Formatter;

public class JsonErrException extends RuntimeException {


	
	public JsonErrException() {
	    super("Json parse error");
	}

	@SuppressWarnings("resource")
	public JsonErrException(String format, Object... args) {
	    super(new Formatter().format(format, args).toString());
	}
}
