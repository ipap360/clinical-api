package com.team360.hms.admissions.web;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class WebServerManager {

    private static WebServerManager instance;

    @Getter
    private WebConfig conf;

    private HttpServer server;

    private WebServerManager(ResourceConfig rc, WebConfig conf) throws URISyntaxException {

        if (server != null) {
            return;
        }

        this.conf = conf;

        URI uri = new URI(conf.getUri());

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        log.info(String.format("Jersey app started with WADL available at " + "%s/application.wadl", uri));
        //} catch (Exception e) {
        //	log.error("An error occured while trying to start the HTTP server!", e);
        //}
    }

    public static void start(ResourceConfig rc, WebConfig conf) throws URISyntaxException {
        instance = new WebServerManager(rc, conf);
    }

    public static void stop() {
        if (instance != null) {
            instance.destroy();
        }
        instance = null;
    }

    public static WebConfig get() {
        return instance.getConf();
    }

    public void destroy() {
        if (server == null) {
            return;
        }

        server.shutdownNow();
    }

}
