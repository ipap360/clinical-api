package com.team360.hms.web;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.net.URI;

@Slf4j
public class WebServerManager {

    private static WebServerManager instance;

    @Getter
    private WebServerConfig config;

    private HttpServer server;

    private WebServerManager(WebServerConfig config) {

        this.config = config;

        if (server != null) {
            return;
        }

        // try {
        final ResourceConfig rc = new ResourceConfig()
//                .packages("com.team360.hms.web.filters")
                .packages(getConfig().getEndpoints(), "com.team360.hms.web.filters", "com.team360.hms.web.mappers")
                //	.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                //			LoggingFeature.Verbosity.PAYLOAD_ANY))
                //	.register(UsersEndpoint.class)
                //	.register(RegistrationsEndpoint.class)

//                .register(RuntimeExceptionMapper.class)
//                .register(GenericExceptionMapper.class)
                .property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(getConfig().getBaseURI()), rc);
        log.debug(String.format("Jersey app started with WADL available at " + "%s/application.wadl", getConfig().getBaseURI()));
        //} catch (Exception e) {
        //	log.error("An error occured while trying to start the HTTP server!", e);
        //}
    }

    public static void start(WebServerConfig config) {
        instance = new WebServerManager(config);
    }

    public static void stop() {
        instance.destroy();
        instance = null;
    }

    public static WebServerConfig get() {
        return instance.getConfig();
    }

    public void destroy() {
        if (server == null) {
            return;
        }

        server.shutdownNow();
    }

}
