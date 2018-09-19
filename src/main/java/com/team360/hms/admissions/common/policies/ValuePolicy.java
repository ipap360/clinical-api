package com.team360.hms.admissions.common.policies;

import com.team360.hms.admissions.common.exceptions.PolicyViolationException;

public interface ValuePolicy<T> {

    default void apply(T t) throws PolicyViolationException {

    }

}
