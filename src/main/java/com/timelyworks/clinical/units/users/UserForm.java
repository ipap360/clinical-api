package com.timelyworks.clinical.units.users;

import com.timelyworks.clinical.common.exceptions.FormValidationException;
import com.timelyworks.clinical.common.policies.LongPasswordPolicy;
import lombok.Data;

import java.util.HashMap;

@Data
public class UserForm {

    private Integer id;

    private String username;

    private String password;

    private String locale;

    private String timezone;

    UserForm validate() {
        HashMap<String, String> errors = new HashMap();

        if (getUsername() == null) {
            errors.put("name", "Please specify a username");
        }

        new LongPasswordPolicy().apply(getPassword());

        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }

        return this;
    }

}
