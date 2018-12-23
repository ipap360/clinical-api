package com.timelyworks.clinical.units.password;

import com.timelyworks.clinical.common.values.EmailAddress;
import lombok.Data;

@Data
public class ForgotPasswordForm {

    private String email;

    private String url;

    public void validate() {
        EmailAddress.from(getEmail());
    }
}
