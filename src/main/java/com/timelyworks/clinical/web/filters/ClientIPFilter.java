package com.timelyworks.clinical.web.filters;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.server.Request;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Slf4j
@PreMatching
@Provider
public class ClientIPFilter implements ContainerRequestFilter, IFilter {

    private static final String X_REAL_IP = "x-real-ip";
    private static final String X_FWD_FOR = "x-forwarded-for";

    @Context
    private javax.inject.Provider<Request> gRequestProvider;

    @Context
    private javax.inject.Provider<HttpHeaders> headersProvider;

    @Override
    public void filter(ContainerRequestContext request) throws IOException {

        final Request grizzlyRequest = gRequestProvider.get();
        final HttpHeaders headers = headersProvider.get();

        String clientIp = headers.getHeaderString(X_REAL_IP);
        if (clientIp == null || clientIp.isEmpty()) {
            // extract of forward ips
            String ipForwarded = headers.getHeaderString(X_FWD_FOR);
            String[] ips = ipForwarded == null ? null : ipForwarded.split(",");
            clientIp = (ips == null || ips.length == 0) ? null : ips[0];

            // extract of remote addr
            clientIp = (clientIp == null || clientIp.isEmpty()) ? grizzlyRequest.getRemoteAddr() : clientIp;
        }

        log.debug("Real Ip[{}]", clientIp);
        request.getHeaders().add(IP_HEADER, clientIp);
    }

}