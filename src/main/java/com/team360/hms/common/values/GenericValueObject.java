package common.values;

import common.exceptions.PolicyViolationException;
import common.policies.ValuePolicy;
import lombok.Getter;

public class GenericValueObject<T> implements ValueObject<T> {

    @Getter
    private T value;

    private ValuePolicy<T> policy;

    GenericValueObject(T value, ValuePolicy<T> policy) throws PolicyViolationException {
        if (policy != null) {
            policy.apply(value);
        }
        this.value = value;
    }

    public String toString() {
        return (String) value;
    }

}
