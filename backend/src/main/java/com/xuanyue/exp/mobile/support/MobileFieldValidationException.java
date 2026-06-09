package com.xuanyue.exp.mobile.support;

import com.xuanyue.exp.mobile.dto.FieldValidationErrorDto;

import java.util.Collections;
import java.util.List;

public class MobileFieldValidationException extends RuntimeException {

    private final List<FieldValidationErrorDto> errors;

    public MobileFieldValidationException(String message, List<FieldValidationErrorDto> errors) {
        super(message);
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public List<FieldValidationErrorDto> getErrors() {
        return errors;
    }
}
