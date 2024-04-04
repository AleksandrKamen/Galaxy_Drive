package com.galaxy.galaxy_drive.validation;

import com.galaxy.galaxy_drive.validation.impl.FieldEqualsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = FieldEqualsValidator.class)
public @interface FieldEquals  {

    String message() default "{error.fields.notMatches}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String field();
    String equalsTo();
}
