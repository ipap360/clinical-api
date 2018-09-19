package com.team360.hms.admissions.units.registration;

import com.team360.hms.admissions.db.DBEntityMeta;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.common.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@DBEntityMeta(name = "REGISTRATIONS", label = "Registration")
public class Registration extends GenericEntity {

    @DBEntityField(name = "EMAIL")
    private String email;

    @DBEntityField(name = "PASSWORD")
    private String password;

    @DBEntityField(name = "IP")
    private String ip;

    @DBEntityField(name = "TOKEN")
    private String token;

    @DBEntityField(name = "LOCALE")
    private String locale;

    @DBEntityField(name = "TIMEZONE")
    private String timezone;

    @DBEntityField(name = "RETRIES")
    private Integer retries = 0;

    @DBEntityField(name = "STATUS")
    private Status status = Status.PENDING;

    @DBEntityField(name = "USER_AGENT")
    private String userAgent;

    public boolean isExpired() {
        return getStatus().equals(Status.EXPIRED);
    }

    public boolean isCompleted() {
        return getStatus().equals(Status.COMPLETED);
    }

    public boolean isPending() {
        return getStatus().equals(Status.PENDING);
    }

    public boolean isTryingHard() {
        return getRetries() > 2;
    }

    public Registration inc() {
        setRetries(getRetries() + 1);
        return this;
    }

    public Registration load(RegistrationForm form) {
        setEmail(form.getEmail());
        setPassword(form.getPassword());
        setTimezone(form.getTimezone());
        return this;
    }

    public enum Status {PENDING, COMPLETED, EXPIRED}

}
