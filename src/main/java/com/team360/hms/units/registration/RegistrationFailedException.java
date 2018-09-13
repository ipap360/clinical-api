package com.team360.hms.units.registration;

import common.exceptions.DomainException;

public class RegistrationFailedException extends DomainException {

    public RegistrationFailedException(String message) {
        super(message);
    }

}
