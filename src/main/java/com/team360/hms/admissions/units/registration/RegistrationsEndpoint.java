package com.team360.hms.admissions.units.registration;

import com.team360.hms.admissions.common.policies.LongPasswordPolicy;
import com.team360.hms.admissions.common.values.EmailAddress;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.RandomToken;
import com.team360.hms.admissions.web.GenericEndpoint;
import com.team360.hms.admissions.common.values.Message;
import com.team360.hms.admissions.web.filters.IFilter;
import com.team360.hms.admissions.units.users.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Path("registrations")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class RegistrationsEndpoint extends GenericEndpoint {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(
            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
            @HeaderParam(IFilter.IP_HEADER) String ip,
            RegistrationForm form) {
        Registration registration = new Registration();
        Optional<Integer> id = new RegistrationDao().findByEmail(form.getEmail());
        if (id.isPresent()) {
            db().read(registration.setId(id.get()));
            if (registration.isPending()) {
                db().update(registration.inc());
                if (registration.isTryingHard()) {
                    // TODO: notify someone. this person is trying again and again without opening their emails
                    // TODO: maybe change the message too
                }
            }
            throw new RuntimeException("A registration with this email address already exists");
        }

        // 1. validate
        String email = EmailAddress.from(form.getEmail()).getValue();
        new LongPasswordPolicy().apply(form.getPassword());

        // 2. send email
        String token = RandomToken.withLength(10).getValue();
        log.debug("Link: " + generateConfirmationLink(form.getUrl(), token, email));
//        sendConfirmationEmail(form.getUrl(), email, token);

        // 3. persist
        registration.setEmail(email);
        registration.setPassword(HashedString.of(form.getPassword()).getValue());
        registration.setIp(ip);
        registration.setUserAgent(userAgent);
        registration.setLocale(form.getLocale());
        registration.setTimezone(form.getTimezone());
        registration.setToken(HashedString.of(token).getValue());

        db().create(registration);

        return Response.ok().entity(new Message("Thank you for signing up!")).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/confirm")
    public Response confirm(RegistrationConfirmForm form) {

        JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(form.getToken())));

        String key = payload.optString("token");
        String email = payload.optString("email");

        Optional<Integer> id = new RegistrationDao().findByEmail(email);
        if (!id.isPresent()) {
            throw new RuntimeException("Bad request");
        }

        Registration registration = new Registration();
        db().read(registration.setId(id.get()));

        HashedString hashedToken = HashedString.fromHash(registration.getToken());
        boolean isTokenValid = hashedToken.isHashOf(key);
        if (!isTokenValid) {
            throw new RuntimeException("Bad request");
        }

        if (registration.isExpired()) {
            throw new RuntimeException("Your previous registration has expired due to inactivity. Please try registering again. It only requires a couple of clicks");
        }

        if (registration.isCompleted()) {
            return Response.ok(new Message("You are already registered in our application")).build();
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

        db().upsert(registration, user);

        return Response.status(Response.Status.CREATED).entity(new Message("Your registration has been successfully completed!")).build();

    }

    private String generateConfirmationLink(String url, String key, String email) {
        String token = Base64.getUrlEncoder()
                .encodeToString((new JSONObject())
                        .put("token", key)
                        .put("email", email)
                        .toString()
                        .getBytes());

        return url.replace(":token", token);
    }

/*    private void sendConfirmationEmail(String link) throws MailException {

        String body = String.format("Please follow this link: " + url);

        log.debug("Registration link: " + url);

        EmailUtils.noReply(mail -> mail
                .to(email)
                .withSubject("Thank you for registering")
                .withPlainText(body)
                .withHTMLText(body));

        EmailUtils.noReply(mail -> mail
                .to(Main.ADMIN)
                .withSubject(String.format("%s just registered to our app!", email))
                .withPlainText(body)
                .withHTMLText(body), true);
    }*/

}
