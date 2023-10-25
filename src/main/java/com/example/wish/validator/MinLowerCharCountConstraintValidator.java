package com.example.wish.validator;

import com.example.wish.annotation.MinLowerCharCount;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class MinLowerCharCountConstraintValidator implements ConstraintValidator<MinLowerCharCount, CharSequence> {

    private int minValue;

    @Override
    public void initialize(MinLowerCharCount constraintAnnotation) {
        this.minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        int count = 0;
        for (int i = 0; i < charSequence.length(); i++) {
            if (Character.isLowerCase(charSequence.charAt(i))) {
                count++;
            }
        }
        return count >= minValue;
    }
}