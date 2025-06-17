package com.easyterview.wingterview.user.validator.annotation;

import com.easyterview.wingterview.user.validator.UserBasicInfoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserBasicInfoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserBasicInfo {

    String message() default "UserBasicInfoDto 유효성 검사 실패";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
