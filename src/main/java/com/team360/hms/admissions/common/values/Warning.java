package com.team360.hms.admissions.common.values;

import lombok.Value;

@Value
public class Warning {

    private String title;

    private String body;

    private String variable;
}
