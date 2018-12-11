package com.team360.hms.admissions.web.filters;

import com.team360.hms.admissions.common.exceptions.AuthenticationException;
import com.team360.hms.admissions.web.AccessToken;
import com.team360.hms.admissions.web.TokenBasedSecurityContext;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Slf4j
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, IFilter {

    @Context
    private javax.inject.Provider<HttpHeaders> headersProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String auth = headersProvider.get().getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException(AuthenticationException.FAILED, "Please sign in to access this resource");
        }
        AccessToken accessToken = AccessToken.parse(auth.substring(BEARER_PREFIX.length()));
        boolean isSecure = requestContext.getSecurityContext().isSecure();
        requestContext.setSecurityContext(new TokenBasedSecurityContext(accessToken, isSecure));
    }

}
