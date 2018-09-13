package com.team360.hms.units.registration;

import common.policies.LongPasswordPolicy;
import common.values.EmailAddress;
import common.values.HashedString;
import common.values.RandomToken;
import com.team360.hms.db.DBManager;
import lombok.extern.slf4j.Slf4j;
import com.team360.hms.units.users.User;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Path("registrations")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class RegistrationsEndpoint {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegistrationForm form) {
        Registration registration = new Registration();
        Integer id = new RegistrationDao().findByEmail(form.getEmail());
        if (id != null) {
            DBManager.utils().read(registration.setId(id));
            if (registration.isPending()) {
                DBManager.utils().update(registration.inc());
                if (registration.isTryingHard()) {
                    // TODO: notify someone. this person is trying again and again without opening their emails
                    // TODO: maybe change the message too
                }
            }
            throw new RegistrationFailedException("A registration with this email address already exists");
        }

        // 1. validate
        String email = EmailAddress.from(form.getEmail()).getValue();
        new LongPasswordPolicy().apply(form.getPassword());

        // 2. send email
        String token = RandomToken.withLength(10).getValue();
//        sendConfirmationEmail(form.getUrl(), email, token);

        // 3. persist
        registration.setEmail(email);
        registration.setPassword(HashedString.of(form.getPassword()).getValue());
        registration.setIp(form.getIp());
        registration.setUserAgent(form.getUserAgent());
        registration.setLocale(form.getLocale());
        registration.setTimezone(form.getTimezone());
        registration.setToken(HashedString.of(token).getValue());

        DBManager.utils().create(registration);

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/confirm")
    public Response confirm(@QueryParam("token") String token) {

        JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(token)));

        String key = payload.optString("token");
        String email = payload.optString("email");

        Integer id = new RegistrationDao().findByEmail(email);
        if (id == null) {
            throw new RuntimeException("Bad request");
        }

        Registration registration = new Registration();
        DBManager.utils().read(registration.setId(id));

        HashedString hashedToken = HashedString.fromHash(registration.getToken());
        boolean isTokenValid = hashedToken.isHashOf(key);
        if (!isTokenValid) {
            throw new RuntimeException("Bad request");
        }

        if (registration.isExpired()) {
            throw new RuntimeException("Your previous registration has expired due to inactivity. Please try registering again. It only requires a couple of clicks");
        }

        if (registration.isCompleted()) {
//            return new RegistrationConfirmed("Your registration has already been confirmed!");
            return Response.ok().build();
        }

        registration.setStatus(Registration.Status.COMPLETED);

        User user = new User();

        user.setUsername(registration.getEmail());
        user.setPassword(registration.getPassword());
        user.setLocale(registration.getLocale());
        user.setLanguage(registration.getLocale());
        user.setTimezone(registration.getTimezone());
        user.setRegistrationId(registration.getId());
        user.setUuid(UUID.randomUUID().toString());

        DBManager.utils().upsert(registration, user);

        return Response.status(Response.Status.CREATED).build();

    }

/*    private void sendConfirmationEmail(String urlPrefix, String email, String token) throws MailException {

        String payload = Base64.getUrlEncoder()
                .encodeToString((new JSONObject())
                        .put("token", token)
                        .put("email", email)
                        .toString()
                        .getBytes());

        String url = urlPrefix + payload;

        String body = String.format("Please follow this link: " + url);

        log.debug("Registration link: " + url);

        EmailUtils.noReply(mail -> mail
                .to(email)
                .withSubject("Thank you for registering")
                .withPlainText(body)
                .withHTMLText(body));

        EmailUtils.noReply(mail -> mail
                .to(com.team360.hms.Main.ADMIN)
                .withSubject(String.format("%s just registered to our app!", email))
                .withPlainText(body)
                .withHTMLText(body), true);
    }

    private void notifyAdminFailure(String msg) {
        EmailUtils.noReply(mail -> mail
                .to(com.team360.hms.Main.ADMIN)
                .withSubject("Failed registration! :-(")
                .withPlainText(msg)
                .withHTMLText(msg), true);
    }*/
}
