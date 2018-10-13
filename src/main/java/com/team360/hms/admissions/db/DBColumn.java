package com.team360.hms.admissions.db;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DBColumn {

    private String name;

    private int type;

    private int size;

    private int decimals;

    private boolean notNull;

}
