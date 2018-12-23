package com.timelyworks.clinical.common.policies;

import com.timelyworks.clinical.common.exceptions.PolicyViolationException;

public interface ValuePolicy<T> {

    default void apply(T t) throws PolicyViolationException {

    }

}
