package com.team360.hms.admissions.common.exceptions;

import lombok.Getter;

import java.util.Map;

public class FormValidationException extends RuntimeException {

    private static final long serialVersionUID = 9076233919931550063L;

    @Getter
    Map<String, String> errors;

    public FormValidationException(Map errors, String message) {
        super(message);
        this.errors = errors;
    }

    public FormValidationException(String message) {
        super(message);
    }

    public FormValidationException(Map errors) {
        super("");
        this.errors = errors;
    }

}
