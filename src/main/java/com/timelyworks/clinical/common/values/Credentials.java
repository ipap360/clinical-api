package com.timelyworks.clinical.common.values;

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
