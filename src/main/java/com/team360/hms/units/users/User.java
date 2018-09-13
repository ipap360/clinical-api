package com.team360.hms.units.users;

import com.team360.hms.db.DBEntityField;
import com.team360.hms.db.DBEntityMeta;
import com.team360.hms.db.GenericEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DBEntityMeta(name="USERS", label="User")
public class User extends GenericEntity {

    @DBEntityField(name="UUID")
    private String uuid;

    @DBEntityField(name="USERNAME")
    private String username;

    @DBEntityField(name="PASSWORD")
    private String password;

    @DBEntityField(name="LANGUAGE")
    private String language;

    @DBEntityField(name="LOCALE")
    private String locale;

    @DBEntityField(name="TIMEZONE")
    private String timezone;

    @DBEntityField(name="REGISTRATION_ID")
    private Integer registrationId;

}
