package com.team360.hms.admissions.web.filters;

import com.team360.hms.admissions.web.AccessToken;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

//import javax.inject.Inject;

@Slf4j
//@Secured
//@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter, IFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
//        Integer userId = 0;
//        Object accessToken = requestContext.getProperty(JWT_PROP);
//        if (accessToken instanceof AccessToken) {
//            AccessToken ac = (AccessToken) accessToken;
//            userId = ac.getUserId();
//            requestContext.setProperty("locale", ac.getLocale());
//            requestContext.setProperty("timezone", ac.getTimezone());
//        }
//        requestContext.setProperty("userId", userId);
    }

}
