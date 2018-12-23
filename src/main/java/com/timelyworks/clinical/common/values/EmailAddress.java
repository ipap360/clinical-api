package com.timelyworks.clinical.common.values;

import com.timelyworks.clinical.common.exceptions.ValueFormatException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
