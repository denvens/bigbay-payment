package com.qingclass.bigbay.tool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;

public class AllowedStrValuesValidator implements ConstraintValidator<AllowedStrValues,String>  {
	
	private String[] allowedValues;

	@Override
	public void initialize(AllowedStrValues constraintAnnotation) {
		this.allowedValues = constraintAnnotation.allowedValues();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(ArrayUtils.contains(allowedValues, value)) {
			return true;
		}
		return false;
	}

	

}
