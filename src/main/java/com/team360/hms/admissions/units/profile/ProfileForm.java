package com.team360.hms.admissions.units.profile;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import com.team360.hms.admissions.units.users.User;
import lombok.Data;

import java.util.HashMap;

@Data
public class ProfileForm {

    private String locale;

    private String timezone;

    ProfileForm validate() {
        HashMap<String, String> errors = new HashMap();

        if (getLocale() == null) {
            errors.put("locale", "Please select a locale");
        }

        if (getTimezone() == null) {
            errors.put("timezone", "Please select a timezone");
        }

        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }


        return this;
    }

    ProfileForm load(User user) {
        setLocale(user.getLocale());
        setTimezone(user.getTimezone());
        return this;
    }

}
