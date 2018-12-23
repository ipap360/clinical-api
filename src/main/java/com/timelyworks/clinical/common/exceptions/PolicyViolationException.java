package com.timelyworks.clinical.common.exceptions;

public class PolicyViolationException extends RuntimeException {

    private static final long serialVersionUID = 24215948655589164L;

    public PolicyViolationException(String message) {
        super(message);
    }

}
