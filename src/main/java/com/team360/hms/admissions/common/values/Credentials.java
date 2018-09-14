package com.team360.hms.admissions.common.values;

import lombok.Value;

@Value
public class Credentials {

    private Integer value;

    public Credentials(String value) {
        this.value = Integer.valueOf(value);
    }

    public Credentials(Integer value) {
        this.value = value;
    }

}
