package com.team360.hms.admissions.units.password;

import com.team360.hms.admissions.common.policies.LongPasswordPolicy;
import lombok.Data;

@Data
public class ResetPasswordForm {

    private String token;

    private String password;

    public ResetPasswordForm validate() {

        new LongPasswordPolicy().apply(getPassword());

        return this;
    }

}
