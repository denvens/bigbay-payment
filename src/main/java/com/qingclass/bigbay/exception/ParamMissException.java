package com.qingclass.bigbay.exception;

import java.util.Formatter;

public class ParamMissException extends RuntimeException {


	
	public ParamMissException() {
	    super("param miss error");
	}

	@SuppressWarnings("resource")
	public ParamMissException(String format, Object... args) {
	    super(new Formatter().format(format, args).toString());
	}
}
