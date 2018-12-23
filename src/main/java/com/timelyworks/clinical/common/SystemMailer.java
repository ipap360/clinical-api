package com.timelyworks.clinical.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

@Slf4j
public class SystemMailer {

    @Getter
    private static SystemMailer instance;

    @Getter
    private Mailer mailer;

    @Getter
    private String address;

    private SystemMailer(Mailer mailer, String address) {
        this.mailer = mailer;
        this.address = address;
    }

    public static void init(String host, int port, String transport, String email, String pass, boolean debug) {
        Mailer mailer = MailerBuilder
                .withSMTPServer(host, port, email, pass)
                .withTransportStrategy(TransportStrategy.valueOf(transport))
//                .trustingAllHosts(true)
                .withSessionTimeout(10 * 1000)
                .withDebugLogging(debug)
                .buildMailer();

        try {
            mailer.testConnection();
            instance = new SystemMailer(mailer, email);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public static void send(Email email, boolean async) {
        if (instance == null) {
            throw new RuntimeException("The system mailer has not been initialized!");
        }

        getInstance().getMailer().sendMail(email, async);
    }

    public static String getFromAddress() {
        return getInstance().getAddress();
    }

}
