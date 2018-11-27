package com.team360.hms.admissions;

import com.team360.hms.admissions.common.values.RandomToken;
import com.team360.hms.admissions.db.DB;
import com.team360.hms.admissions.db.DBManagerConfig;
import com.team360.hms.admissions.web.MyObjectMapperProvider;
import com.team360.hms.admissions.web.WebConfig;
import com.team360.hms.admissions.web.WebServerManager;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class Main {

    private static final String DEFAULT_BUILD_MODE = "DEV";
    private static final String DEFAULT_LOG_LEVEL = "DEBUG";

    public static final String BUILD_MODE = "BUILD_MODE";
    public static final String LOG_LEVEL = "LOG_LEVEL";

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String DEFAULT_DB_NAME = "bed_management_db";
    private static final String DEFAULT_DB_CONFIG = "?zeroDateTimeBehavior=CONVERT_TO_NULL&createDatabaseIfNotExist=true&useSSL=false&characterEncoding=UTF8";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/" + DEFAULT_DB_NAME + DEFAULT_DB_CONFIG;
    private static final String DEFAULT_DB_USER = "root";

    public static final String SQL_URL = "SQL_URL";
    public static final String SQL_DRIVER = "SQL_DRIVER";
    public static final String SQL_USER = "SQL_USER";
    public static final String SQL_PASSWORD = "SQL_PASSWORD";

    public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

    private static final String DEFAULT_IS_SECURE = "false";
    private static final String DEFAULT_URI = "http://0.0.0.0:8080";
    private static final String DEFAULT_DOMAIN_NAME = "localhost";
    private static final String DEFAULT_CONTEXT = "";

    private static final String DEFAULT_ACCESS_TIMEOUT = String.valueOf(20 * 60);
    private static final String DEFAULT_REFRESH_TIMEOUT = String.valueOf(7 * 24 * 60 * 60);
    private static final String DEFAULT_ACCESS_COOKIE = "aou8";
    private static final String DEFAULT_REFRESH_COOKIE1 = "keep4live";
    private static final String DEFAULT_REFRESH_COOKIE2 = "cli3ntRT";

    public static final String IS_SECURE = "IS_SECURE";
    public static final String BASE_URI = "BASE_URI";
    public static final String DOMAIN_NAME = "DOMAIN_NAME";
    public static final String API_CONTEXT = "API_CONTEXT";

    public static final String ACCESS_TIMEOUT = "ACCESS_TIMEOUT";
    public static final String ACCESS_COOKIE = "ACCESS_COOKIE";

    public static final String REFRESH_TIMEOUT = "REFRESH_TIMEOUT";
    public static final String REFRESH_SERVER_COOKIE = "REFRESH_SERVER_COOKIE";
    public static final String REFRESH_CLIENT_COOKIE = "REFRESH_CLIENT_COOKIE";

    public static final String SIGNATURE_KEY = "SIGNATURE_KEY";
    public static final String ADMIN = "ADMIN";

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

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("database/hikari.mysql.properties")) {

            Properties props = new Properties();
            props.load(input);

            log.debug(props.toString());

            // defaults
            Map<String, String> opts = new HashMap();

            opts.put(BUILD_MODE, DEFAULT_BUILD_MODE);
            opts.put(LOG_LEVEL, DEFAULT_LOG_LEVEL);

            opts.put(SQL_DRIVER, DEFAULT_DRIVER);
            opts.put(SQL_URL, DEFAULT_DB_URL);
            opts.put(SQL_USER, DEFAULT_DB_USER);
            opts.put(SQL_PASSWORD, DEFAULT_DB_USER);

            opts.put(IS_SECURE, DEFAULT_IS_SECURE);
            opts.put(BASE_URI, DEFAULT_URI);
            opts.put(DOMAIN_NAME, DEFAULT_DOMAIN_NAME);
            opts.put(API_CONTEXT, DEFAULT_CONTEXT);

            opts.put(ACCESS_TIMEOUT, DEFAULT_ACCESS_TIMEOUT);
            opts.put(REFRESH_TIMEOUT, DEFAULT_REFRESH_TIMEOUT);
            opts.put(ACCESS_COOKIE, DEFAULT_ACCESS_COOKIE);

            opts.put(REFRESH_SERVER_COOKIE, DEFAULT_REFRESH_COOKIE1);
            opts.put(REFRESH_CLIENT_COOKIE, DEFAULT_REFRESH_COOKIE2);

            // a failure to provide a permanent signature key will result in logging out everyone in every server restart
            opts.put(SIGNATURE_KEY, RandomToken.withLength(80).getValue());

            Map<String, String> env = System.getenv();
            opts.putAll(env);

            List<String> encrypted = new ArrayList();
            encrypted.add("PATIENTS.NOTES");
            encrypted.add("ADMISSIONS.NOTES");

            DBManagerConfig db = DBManagerConfig.builder()
                    .properties(props)
                    .url(opts.get(SQL_URL))
                    .driver(opts.get(SQL_DRIVER))
                    .user(opts.get(SQL_USER))
                    .pass(opts.get(SQL_PASSWORD))
                    .enc(opts.get(ENCRYPTION_KEY))
                    .encrypted(encrypted)
                    .migrations("database/liquibase-master.xml")
                    .build();

            DB.start(db);

//            EmailUtils.test();
            final WebConfig conf = WebConfig.builder()
                    .isSecure(Boolean.valueOf(opts.get(IS_SECURE)))
                    .uri(opts.get(BASE_URI))
                    .domainName(opts.get(DOMAIN_NAME))
                    .context(opts.get(API_CONTEXT))
                    .secret(opts.get(SIGNATURE_KEY))
                    .admin(opts.get(ADMIN))
                    .accessTokenTimeout(Integer.valueOf(opts.get(ACCESS_TIMEOUT)))
                    .refreshTokenTimeout(Integer.valueOf(opts.get(REFRESH_TIMEOUT)))
                    .accessTokenCookie(opts.get(ACCESS_COOKIE))
                    .refreshTokenServerCookie(opts.get(REFRESH_SERVER_COOKIE))
                    .refreshTokenClientCookie(opts.get(REFRESH_CLIENT_COOKIE))
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

            if (opts.get(BUILD_MODE).equals(DEFAULT_BUILD_MODE)) {
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
