package com.team360.hms.web.filters;

import common.exceptions.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import com.team360.hms.web.WebServerManager;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Provider
@Secured
@Priority(Priorities.AUTHENTICATION - 2)
public class AntiXSRFFilter implements ContainerRequestFilter, IFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Map<String, Cookie> cookies = requestContext.getCookies();
        Cookie accessCookie = cookies.get(ACCESS_TOKEN_COOKIE_NAME);

        if (accessCookie == null) {
            return;
        }

        String cookie = WebServerManager.get().getXsrfCookie();
        Cookie xsrfCookie = cookies.get(cookie);
        if (xsrfCookie == null) {
            log.debug("Cookie " + cookie + " not found");
            throw new AuthenticationException(AuthenticationException.FAILED);
        }

        cookie = xsrfCookie.getValue();

        String header = WebServerManager.get().getXsrfHeader();
        header = requestContext.getHeaderString(header);

        log.debug("Header: " + header + ", Cookie: " + cookie);
        if (header == null || !header.equals(cookie)) {
            throw new AuthenticationException(AuthenticationException.FAILED);
        }

    }
}
