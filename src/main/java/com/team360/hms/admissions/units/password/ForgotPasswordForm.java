package com.team360.hms.admissions.units.password;

import com.team360.hms.admissions.common.values.EmailAddress;
import lombok.Data;

@Data
public class ForgotPasswordForm {

    private String email;

    private String url;

    public void validate() {
        EmailAddress.from(getEmail());
    }
}
