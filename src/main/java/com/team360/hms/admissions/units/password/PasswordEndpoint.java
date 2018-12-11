package com.team360.hms.admissions.units.password;

import com.team360.hms.admissions.common.SystemMailer;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.common.values.Message;
import com.team360.hms.admissions.common.values.RandomToken;
import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.units.users.User;
import com.team360.hms.admissions.units.users.UserDao;
import com.team360.hms.admissions.web.filters.IFilter;
import lombok.extern.log4j.Log4j2;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.team360.hms.admissions.units.password.PasswordResetRequest.Status.*;

@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("password")
public class PasswordEndpoint {

    @Context
    ContainerRequestContext crc;

    @POST
    @Path("/forgot")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response forgot(@HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
                           @HeaderParam(IFilter.IP_HEADER) String ip,
                           ForgotPasswordForm form) {

        // 1) check if email is valid. will throw if not valid and stop here
        form.validate();

        final PasswordDao passwordDao = new PasswordDao();

        // 2) check if there is already a password reset request
        List<String> statuses = Arrays.asList(PENDING.name(), VERIFIED.name(), SENT.name());
        Optional<Integer> id = passwordDao.findActiveByEmailAndStatus(form.getEmail(), statuses);
        if (id.isPresent()) {
            throw new RuntimeException("You have already requested a password reset using this email address. Please check your inbox");
        }

        // 3) check if this IP has been tooooo active with fake emails
        Instant since = Instant.now().minus(10, ChronoUnit.MINUTES);
        Integer count = passwordDao.countBySameIpStatusCreation(ip, PENDING.name(), since);
        if (count > 2) {
            throw new RuntimeException("You have sent too many password reset requests. Please try again later");
        }

        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail(form.getEmail());
        req.setIp(ip);
        req.setUserAgent(userAgent);
        req.setExpiresAt(Instant.now().plus(20, ChronoUnit.MINUTES));
        req.setStatus(PENDING);
        WebUtl.db(crc).upsert(req);

        final UserDao userDao = new UserDao();
        Optional<Integer> userId = userDao.findByUsername(form.getEmail());
        if (userId.isPresent()) {
            req.setStatus(VERIFIED);
            WebUtl.db(crc).upsert(req);

            String key = RandomToken.withLength(14).getValue();
            String token = Base64.getUrlEncoder()
                    .encodeToString((key + "." + req.getId()).getBytes());

            Email email = EmailBuilder
                    .startingBlank()
                    .from("Clinic360", SystemMailer.getFromAddress())
                    .to(form.getEmail())
                    .withSubject("Somebody requested a new password for your clinic360 account")
                    .withPlainText("To reset your password, please follow the link bellow " + form.getUrl().replace(":token", token))
                    .buildEmail();
            try {
                SystemMailer.send(email, false);
                req.setToken(HashedString.of(key).getValue());
                req.setStatus(SENT);
                WebUtl.db(crc).upsert(req);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return Response.ok().entity(new Message("Your request has been processed! You will shortly receive an email with further instructions on how to reset your password. \nIf the email doesn't arrive within a few minutes, please check your spam folder and make sure that this is the email address that you use to access our service")).build();
    }

    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reset(ResetPasswordForm form) {
        form.validate();
        String token = new String(Base64.getUrlDecoder().decode(form.getToken().getBytes()));
        PasswordResetRequest req = new PasswordResetRequest();
        int i = token.lastIndexOf(".");
        String key = token.substring(0, i);
        Integer id = Integer.valueOf(token.substring(i+1));
        WebUtl.db(crc).read(req.setId(id));
        if (!req.isSent()) {
            throw new RuntimeException("Bad request");
        }

        HashedString hashedToken = HashedString.fromHash(req.getToken());
        boolean isTokenValid = hashedToken.isHashOf(key);
        if (!isTokenValid) {
            throw new RuntimeException("Bad request");
        }

        final UserDao userDao = new UserDao();
        Optional<Integer> userId = userDao.findByUsername(req.getEmail());
        if (!userId.isPresent()) {
            throw new RuntimeException("Bad request");
        }

        User user = new User();
        WebUtl.db(crc).read(user.setId(userId.get()));

        req.setStatus(COMPLETED);
        WebUtl.db(crc).upsert(user.load(form), req);

        return Response.ok().entity(new Message("Your password has been successfully updated")).build();
    }

    @POST
    @Path("/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancel() {
        return Response.ok().build();
    }

}
