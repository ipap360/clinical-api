package com.team360.hms.admissions.units.profile;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import com.team360.hms.admissions.common.exceptions.PolicyViolationException;
import com.team360.hms.admissions.common.policies.LongPasswordPolicy;
import com.team360.hms.admissions.common.values.HashedString;
import com.team360.hms.admissions.units.users.User;
import lombok.Data;

import java.util.HashMap;

@Data
public class ChangePasswordForm {

    private String oldPassword;

    private String newPassword;

    ChangePasswordForm validate(User user) {
        HashMap<String, String> errors = new HashMap();

        if (getOldPassword() == null) {
            errors.put("oldPassword", "Please type your current password");
        } else {
            HashedString pass = HashedString.fromHash(user.getPassword());
            if (!pass.isHashOf(getOldPassword())) {
                errors.put("oldPassword", "Your current password is not correct");
            }
        }

        if (getNewPassword() == null) {
            errors.put("newPassword", "Please choose a valid password");
        } else {
            try {
                new LongPasswordPolicy().apply(getNewPassword());
            } catch (PolicyViolationException e) {
                errors.put("newPassword", e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }

        return this;
    }

}
