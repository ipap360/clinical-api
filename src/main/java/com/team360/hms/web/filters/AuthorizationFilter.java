package com.team360.hms.web.filters;

import lombok.extern.slf4j.Slf4j;
import com.team360.hms.web.AccessToken;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Slf4j
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter, IFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        AccessToken accessToken = (AccessToken) requestContext.getProperty(JWT_PROP);

//        requestContext.setSecurityContext(new SecurityContext() {
//            @Override
//            public Principal getUserPrincipal() {
//                return new Principal() {
//                    @Override
//                    public String getName() {
//                        return String.valueOf(accessToken.getUserId());
//                    }
//                };
//            }
//
//            @Override
//            public boolean isUserInRole(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean isSecure() {
//                return false;
//            }
//
//            @Override
//            public String getAuthenticationScheme() {
//                return null;
//            }
//        });

        requestContext.getHeaders().add(USER_ID_HEADER, String.valueOf(accessToken.getUserId()));
    }

}
