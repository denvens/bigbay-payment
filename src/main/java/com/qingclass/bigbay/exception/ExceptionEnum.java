package com.qingclass.bigbay.exception;

public enum ExceptionEnum {
	
	JSON_PARSE_ERR_EXCEPTION(100000, "JSON解析错误"),
	
	ARGUMENTS_VALIDATE_EXCEPTION(200000, "请求参数错误"),
	

	NO_LOGIN_EXCEPTION(300000, "未登陆");
	
	

    private int code;

    private String message;

    ExceptionEnum(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
