package com.team360.hms.admissions.common.values;

import com.team360.hms.admissions.common.exceptions.ValueFormatException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator;

@Slf4j
@Value
public class EmailAddress {

    private String value;

    public static EmailAddress from(String email) {

        if (StringUtils.isEmpty(email)) {
            throw new ValueFormatException("Please provide a valid email address");
        }

        if (!EmailAddressValidator.isValidStrict(email)) {
            throw new ValueFormatException(String.format("%s is not a valid email address", email));
        }

        return new EmailAddress(email);
    }

    public String toString() {
        return value;
    }

}
