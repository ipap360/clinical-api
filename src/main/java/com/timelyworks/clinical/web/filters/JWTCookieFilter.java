package com.timelyworks.clinical.web.filters;

import com.timelyworks.clinical.web.WebServerManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Slf4j
@Provider
@Priority(Priorities.AUTHENTICATION - 1)
public class JWTCookieFilter implements ContainerRequestFilter, IFilter {

    @Context
    private javax.inject.Provider<HttpHeaders> headersProvider;

    @Context
    private javax.inject.Provider<UriInfo> infoProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final HttpHeaders headers = headersProvider.get();

        // 1. try cookie based sessions
        Map<String, Cookie> cookies = headers.getCookies();
        Cookie cookie = cookies.get(WebServerManager.get().getAccessTokenCookie());
        if (cookie != null) {
            String token = cookie.getValue();
            requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        }
    }

}
