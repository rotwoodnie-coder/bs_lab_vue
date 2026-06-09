package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.xuanyue.exp.mobile")
public class MobileExceptionHandler {

    @ExceptionHandler(MobileFieldValidationException.class)
    public ApiResponse<?> handleFieldValidation(MobileFieldValidationException ex) {
        return ApiResponse.fail(400, ex.getMessage(), ex.getErrors());
    }
}
