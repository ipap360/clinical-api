package com.timelyworks.clinical.common.values;

import lombok.Value;

@Value
public class Warning {

    private String title;

    private String body;

    private String variable;
}
