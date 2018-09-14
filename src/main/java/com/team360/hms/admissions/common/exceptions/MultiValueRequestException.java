package com.team360.hms.admissions.common.exceptions;

import java.util.HashMap;

public class MultiValueRequestException extends RuntimeException {

    private static final long serialVersionUID = 9076233919931550063L;
    HashMap errors;

    public MultiValueRequestException(HashMap errors, String message) {
        super(message);
        this.errors = errors;
    }

    public MultiValueRequestException(String message) {
        super(message);
    }

    public MultiValueRequestException(HashMap errors) {
        super("");
        this.errors = errors;
    }

}
