package com.timelyworks.clinical.common.exceptions;

public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 6195792856528720020L;

    public DomainException(String message) {
        super(message);
    }

}
