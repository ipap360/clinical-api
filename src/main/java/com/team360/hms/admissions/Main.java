package com.team360.hms.admissions;

import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBManagerConfig;
import com.team360.hms.admissions.web.MyObjectMapperProvider;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.WebServerManager;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@Slf4j
public class Main {

    public static final String SQL_TYPE = "SQL_TYPE";
    public static final String SQL_URL = "SQL_URL";
    public static final String SQL_DRIVER = "SQL_DRIVER";
    public static final String SQL_USER = "SQL_USER";
    public static final String SQL_PASSWORD = "SQL_PASSWORD";
    public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

//    public static final String SYSTEM_EMAIL_ADDRESS = "SYSTEM_EMAIL_ADDRESS";
//    public static final String SYSTEM_EMAIL_PASS = "SYSTEM_EMAIL_PASS";
//    public static final String SYSTEM_EMAIL_SERVER = "SYSTEM_EMAIL_SERVER";
//    public static final String SYSTEM_EMAIL_PORT = "SYSTEM_EMAIL_PORT";

    public static final String SIGNATURE_KEY = "SIGNATURE_KEY";

    public static final String PROTOCOL = "PROTOCOL";
    public static final String PORT = "PORT";
    public static final String DOMAIN = "DOMAIN";
    public static final String CONTEXT = "CONTEXT";

    public static final boolean DEBUG_MODE = java.lang.management.ManagementFactory.getRuntimeMXBean().
            getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Close connections...");
            DB.stop();
        }, "disconnectDB"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping server...");
            WebServerManager.stop();
        }, "shutdownNow"));

        try {

            DBManagerConfig db = DBManagerConfig.builder()
                    .type(System.getenv(SQL_TYPE))
                    .url(System.getenv(SQL_URL))
                    .driver(System.getenv(SQL_DRIVER))
                    .user(System.getenv(SQL_USER))
                    .pass(System.getenv(SQL_PASSWORD))
                    .enc(System.getenv(ENCRYPTION_KEY))
                    .build();

            DB.start(db);

//            EmailUtils.test();
            final WebConfig conf = WebConfig.builder()
                    .protocol(System.getenv(PROTOCOL))
                    .domain(System.getenv(DOMAIN))
                    .port(System.getenv(PORT))
                    .context(System.getenv(CONTEXT))
                    .secret(System.getenv(SIGNATURE_KEY))
//                    .accessTokenTimeout(1 * 20)
                    .build();

            final ResourceConfig rc = new ResourceConfig()
                    .packages("com.team360.hms.admissions")
                    .register(MyObjectMapperProvider.class)
                    .register(JacksonFeature.class)
//                    .register(new AbstractBinder() {
//                        @Override
//                        protected void configure() {
//
//                        }
//                    })
                    .property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);

            WebServerManager.start(rc, conf);

            if (args != null && args.length == 1) {
                System.out.println("Press Enter to exit..");
                System.in.read();
                log.info("Exiting...");
                System.exit(0);
            } else {
                System.out.println("Press Ctrl+C to exit..");
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
            System.exit(0);
        }
    }
}
