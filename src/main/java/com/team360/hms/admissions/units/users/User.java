package com.team360.hms.admissions.units.users;

import com.team360.hms.admissions.common.GenericEntity;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DBEntityMeta(name = "USERS", label = "User")
public class User extends GenericEntity {

    @DBEntityField(name = "UUID")
    private String uuid;

    @DBEntityField(name = "USERNAME")
    private String username;

    @DBEntityField(name = "PASSWORD")
    private String password;

    @DBEntityField(name = "LANGUAGE")
    private String language = "en-US";

    @DBEntityField(name = "LOCALE")
    private String locale = "en-US";

    @DBEntityField(name = "TIMEZONE")
    private String timezone = "UTC";

    @DBEntityField(name = "REGISTRATION_ID")
    private Integer registrationId;


    public User load(UserForm form) {
        setUsername(form.getUsername());
        setPassword(form.getPassword());
        setTimezone(form.getTimezone());
        setLocale(form.getLocale());
        setLanguage(form.getLocale());
        return this;
    }

}
