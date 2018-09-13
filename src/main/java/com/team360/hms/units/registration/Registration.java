package com.team360.hms.units.registration;

import com.team360.hms.db.DBEntityField;
import com.team360.hms.db.DBEntityMeta;
import com.team360.hms.db.GenericEntity;
import lombok.Data;

@Data
@DBEntityMeta(name="REGISTRATIONS", label="Registration")
public class Registration extends GenericEntity {

    public enum Status {PENDING, COMPLETED, EXPIRED}

    @DBEntityField(name="EMAIL")
    private String email;

    @DBEntityField(name="PASSWORD")
    private String password;

    @DBEntityField(name="IP")
    private String ip;

    @DBEntityField(name="TOKEN")
    private String token;

    @DBEntityField(name="LOCALE")
    private String locale;

    @DBEntityField(name="TIMEZONE")
    private String timezone;

    @DBEntityField(name="RETRIES")
    private Integer retries = 0;

    @DBEntityField(name="STATUS")
    private Status status;

    @DBEntityField(name="USER_AGENT")
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

    public Registration inc () {
        setRetries(getRetries() + 1);
        return this;
    }

    public Registration load(RegistrationForm form) {
        setEmail(form.getEmail());
        setPassword(form.getPassword());
        setTimezone(form.getTimezone());
        return this;
    }

    /*
    *
    *     @Override
    public RowMapper<Registration> toEntity() {
        return (ResultSet rs, StatementContext ctx) -> Registration.builder()
                .id(rs.getInt("ID"))
                .createdAt(toInstant(rs.getTimestamp("CREATED_AT")))
                .modifiedAt(toInstant(rs.getTimestamp("MODIFIED_AT")))
                .email(rs.getString("EMAIL"))
                .password(rs.getString("PASSWORD"))
                .retries(rs.getInt("RETRIES"))
                .ip(rs.getString("IP"))
                .userAgent(rs.getString("USER_AGENT"))
                .token(rs.getString("TOKEN"))
                .locale(rs.getString("LOCALE"))
                .timezone(rs.getString("TIMEZONE"))
                .status(rs.getInt("STATUS"))
                .invitationId(rs.getInt("INVITATION_ID"))
                .build();
    }

    @Override
    public Map<String, ?> toMap(Registration r) {

        Map<String, Object> m = new HashMap<>();

        m.put("ID", r.getId());
        m.put("CREATED_AT", r.getCreatedAt());
        m.put("MODIFIED_AT", r.getModifiedAt());
        m.put("EMAIL", r.getEmail());
        m.put("STATUS", r.getStatus());
        m.put("PASSWORD", r.getPassword());
        m.put("RETRIES", r.getRetries());
        m.put("TOKEN", r.getToken());
        m.put("LOCALE", r.getLocale());
        m.put("TIMEZONE", r.getTimezone());
        m.put("IP", r.getIp());
        m.put("USER_AGENT", r.getUserAgent());

        m.put("INVITATION_ID", r.getInvitationId());

        return m;
    }
    *
    *
    * */
}
