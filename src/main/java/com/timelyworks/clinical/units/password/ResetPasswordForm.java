package com.timelyworks.clinical.units.password;

import com.timelyworks.clinical.common.policies.LongPasswordPolicy;
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
