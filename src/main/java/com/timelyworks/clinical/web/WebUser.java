package com.timelyworks.clinical.web;

import lombok.Value;

import java.security.Principal;
import java.time.ZoneId;
import java.util.Locale;

@Value
public class WebUser implements Principal {

    private int id;

    private String name;

    private Locale locale;

    private ZoneId timezone;

    public WebUser(int id, String name, String locale, String timezone) {
        this.id = id;
        this.name = name;
        this.locale = (locale != null) ? Locale.forLanguageTag(locale) : Locale.UK;
        this.timezone = (timezone != null) ? ZoneId.of(timezone) : ZoneId.of("UTC");
    }

    @Override
    public String getName() {
        return name;
    }

}
