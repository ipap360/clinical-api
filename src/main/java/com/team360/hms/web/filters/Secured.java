package com.team360.hms.web.filters;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NameBinding
@Retention(RUNTIME)
public @interface Secured {
}