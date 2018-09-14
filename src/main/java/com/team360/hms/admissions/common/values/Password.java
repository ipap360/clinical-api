package com.team360.hms.admissions.common.values;

import com.team360.hms.admissions.common.policies.LongPasswordPolicy;
import com.team360.hms.admissions.common.policies.ValuePolicy;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@ToString
public class Password extends GenericValueObject<String> {

    private Password(String value, ValuePolicy<String> policy) {
        super(value, policy);
    }

    public static Password from(String value) {
        return new Password(value, new LongPasswordPolicy());
    }

    public static Password from(String value, ValuePolicy<String> policy) {
        return new Password(value, policy);
    }

}
