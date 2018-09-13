package common.utils;

import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.MailException;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.util.function.UnaryOperator;

@Slf4j
public class EmailUtils {

/*    private static final String NO_REPLY_NAME = "Clinic Calendar";
    private static final String NO_REPLY_EMAIL = System.getenv(SYSTEM_EMAIL_ADDRESS);
    private static final String NO_REPLY_PASS = System.getenv(SYSTEM_EMAIL_PASS);

    private static Mailer noReplyMailer;

    private static void initNoReplyMailer() {
        noReplyMailer = MailerBuilder
                .withSMTPServer(
                        System.getenv(SYSTEM_EMAIL_SERVER),
                        Integer.valueOf(System.getenv(SYSTEM_EMAIL_PORT)),
                        NO_REPLY_EMAIL,
                        NO_REPLY_PASS
                )
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .trustingAllHosts(true)
                .withDebugLogging(true)
                .buildMailer();
    }

    private static void maybeInitNoReplyMailer() {
        if (noReplyMailer == null) {
            log.debug("Initializing noReplyMailer...");
            initNoReplyMailer();
            log.debug(noReplyMailer.toString());
        }
    }

    public static void test() {
        maybeInitNoReplyMailer();
        noReplyMailer.testConnection();
    }

    public static void noReply(UnaryOperator<EmailPopulatingBuilder> builder, boolean async) throws MailException {
        maybeInitNoReplyMailer();
        EmailPopulatingBuilder b = EmailBuilder
                .startingBlank()
                .from(NO_REPLY_NAME, NO_REPLY_EMAIL);

        Email email = builder.apply(b).buildEmail();

        log.debug("Email to send: " + email);

        noReplyMailer.sendMail(email, async);
    }

    public static void noReply(UnaryOperator<EmailPopulatingBuilder> builder) throws MailException {
        noReply(builder, false);
    }*/

}
