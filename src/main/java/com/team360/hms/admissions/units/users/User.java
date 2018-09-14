package com.team360.hms.admissions.units.users;

import com.team360.hms.admissions.db.DBEntityMeta;
import com.team360.hms.admissions.common.GenericEntity;
import com.team360.hms.admissions.db.DBEntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@DBEntityMeta(name = "USERS", label = "User")
public class User extends GenericEntity {

    @DBEntityField(name = "UUID")
    private String uuid;

    @DBEntityField(name = "USERNAME")
    private String username;

    @DBEntityField(name = "PASSWORD")
    private String password;

    @DBEntityField(name = "LANGUAGE")
    private String language;

    @DBEntityField(name = "LOCALE")
    private String locale;

    @DBEntityField(name = "TIMEZONE")
    private String timezone;

    @DBEntityField(name = "REGISTRATION_ID")
    private Integer registrationId;

}
