package com.qingclass.bigbay.config;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.qingclass.bigbay.exception.ArgumentsValidateException;
import com.qingclass.bigbay.exception.ExceptionEnum;
import com.qingclass.bigbay.exception.JsonErrException;
import com.qingclass.bigbay.exception.NoLoginException;
import com.qingclass.bigbay.exception.ParamMissException;
import com.qingclass.bigbay.tool.BeanValidators;
import com.qingclass.bigbay.tool.Tools;

import lombok.extern.slf4j.Slf4j;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	/**json 格式错误全局异常处理*/
	@ExceptionHandler(value = JsonErrException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Object jsonErrException(Exception exception,HttpServletResponse response) {
	    String message = exception.getMessage();
	    log.error(message);
	    return f(ExceptionEnum.JSON_PARSE_ERR_EXCEPTION, message);
	}

	/**请求参数校验错误全局异常处理*/
	@ExceptionHandler(value = ArgumentsValidateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Object argumentsValidateException(ArgumentsValidateException e) {
	    List<String> list = BeanValidators.extractPropertyAndMessageAsList(e);
	    return f(ExceptionEnum.ARGUMENTS_VALIDATE_EXCEPTION, list);
	}

	
	
	/**类型转换错误错误全局异常处理*/
	@ExceptionHandler(value = ClassCastException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Object classCastException(ClassCastException e) {
		log.error(ExceptionUtils.getStackTrace(e));
		return f(ExceptionEnum.ARGUMENTS_VALIDATE_EXCEPTION, "类型转换异常");
	}
	/**参数null全局异常处理*/
	@ExceptionHandler(value = ParamMissException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Object paramMissException(ParamMissException e) {
		log.error(e.getMessage(), e);
		return f(ExceptionEnum.ARGUMENTS_VALIDATE_EXCEPTION, e.getMessage());
	}
	
	/**未登陆异常*/
	@ExceptionHandler(value = NoLoginException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Object noLoginException(NoLoginException e) {
		log.error(ExceptionUtils.getMessage(e), e);
		return f(ExceptionEnum.NO_LOGIN_EXCEPTION, e.getMessage());
	}
	
	
	private Object f(ExceptionEnum exceptionEnum, Object object ) {
		return Tools.f(object, exceptionEnum.getCode(), exceptionEnum.getMessage());
	}
}
