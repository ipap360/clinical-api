package com.timelyworks.clinical.units.password;

import com.timelyworks.clinical.common.GenericEntity;
import com.timelyworks.clinical.db.DBEntityField;
import com.timelyworks.clinical.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@DBEntityMeta(name = "PASSWORD_RESET_REQUESTS", label = "Password Reset Request")
public class PasswordResetRequest extends GenericEntity {

    @DBEntityField(name = "EMAIL")
    private String email;

    @DBEntityField(name = "TOKEN")
    private String token;

    @DBEntityField(name = "EXPIRES_AT")
    private Instant expiresAt;

    @DBEntityField(name = "STATUS")
    private Status status = Status.PENDING;

    @DBEntityField(name = "IP")
    private String ip;

    @DBEntityField(name = "USER_AGENT")
    private String userAgent;

    public boolean isSent() {
        return getStatus().equals(Status.SENT);
    }

    public boolean isCompleted() {
        return getStatus().equals(Status.COMPLETED);
    }

    public boolean isPending() {
        return getStatus().equals(Status.PENDING);
    }

    public boolean isCanceled() {
        return getStatus().equals(Status.CANCELED);
    }

    public enum Status {PENDING, VERIFIED, SENT, COMPLETED, CANCELED}

}
