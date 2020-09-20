package com.qingclass.bigbay.tool;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import com.google.common.collect.Lists;
import com.qingclass.bigbay.exception.ArgumentsValidateException;

public class BeanValidators {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void validateWithException(Validator validator, Object object, Class<?>... groups)
			throws ConstraintViolationException {
		Set constraintViolations = validator.validate(object, groups);
		if (!constraintViolations.isEmpty()) {
			throw new ArgumentsValidateException(constraintViolations);
		}
	}

	public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e) {
		return extractPropertyAndMessageAsList(e.getConstraintViolations(), " ");
	}

	@SuppressWarnings("rawtypes")
	public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation> constraintViolations,
			String separator) {
		List<String> errorMessages = Lists.newArrayList();
		for (ConstraintViolation violation : constraintViolations) {
			errorMessages.add(violation.getPropertyPath() + separator + violation.getMessage());
		}
		return errorMessages;
	}

}
