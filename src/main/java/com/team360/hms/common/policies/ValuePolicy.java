package common.policies;

import common.exceptions.PolicyViolationException;

public interface ValuePolicy<T> {

    default void apply(T t) throws PolicyViolationException {

    }

}
