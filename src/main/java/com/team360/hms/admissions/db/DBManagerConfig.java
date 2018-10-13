package com.team360.hms.admissions.db;

import com.google.common.base.CaseFormat;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Properties;

@Value
@Builder
public class DBManagerConfig {

    Properties properties;

    String url;

    String driver;

    String user;

    String pass;

    String enc;

    // liquibase migrations file path
    String migrations;

    List<String> encrypted;

    // default caseformat of database tables/columns
    @Builder.Default
    CaseFormat format = CaseFormat.UPPER_UNDERSCORE;

}
