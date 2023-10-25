package com.example.wish.annotation;


import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Size(min = 6, message = "min size is 6")
@NotNull
@MinDigitCount(message = "min digit count is 1")
@MinUpperCharCount(message = "min upper char count is 1")
@MinLowerCharCount(message = "min lower char count is 1")
@MinSpecCharCount(message = "min special char count is 1")
public @interface PasswordStrength {

    String message() default "PasswordStrength";

    Class<? extends Payload>[] payload() default {};

    Class<?>[] groups() default {};

}