package com.qingclass.bigbay.tool;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;

public class AllowedIntValuesValidator implements ConstraintValidator<AllowedIntValues,Integer>  {
	
	private Integer[] allowedValues;

	@Override
	public void initialize(AllowedIntValues constraintAnnotation) {
		int[] ints = constraintAnnotation.allowedValues();
		this.allowedValues = ArrayUtils.toObject(ints);
	}

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if(ArrayUtils.contains(allowedValues, value)) {
			return true;
		}
		return false;
	}

	

}
