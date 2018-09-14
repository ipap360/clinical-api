package com.team360.hms.admissions.common.policies;

import com.team360.hms.admissions.common.exceptions.PolicyViolationException;

public class LongPasswordPolicy implements ValuePolicy<String> {

    private static final int MIN_LENGTH = 8;

//    private static final String AT_LEAST_ONE_CHAR_REGEX = ".*[a-zA-Z]+.*";
//    private static final String AT_LEAST_ONE_NUMBER_REGEX = ".*\\d.*";
//    private static final String NO_SPECIAL_REGEX = "[a-zA-Z0-9]*";
//
//    private static Pattern charPattern = Pattern.compile(AT_LEAST_ONE_CHAR_REGEX, Pattern.CASE_INSENSITIVE);
//    private static Pattern numPattern = Pattern.compile(AT_LEAST_ONE_NUMBER_REGEX, Pattern.CASE_INSENSITIVE);
//    private static Pattern specPattern = Pattern.compile(NO_SPECIAL_REGEX, Pattern.CASE_INSENSITIVE);

//        private static final String POLICY_MESSAGE= "The password should be at least 8 characters containing at least one letter, one number and one symbol";

    private static final String POLICY_MESSAGE = String.format("The password should be at least %d characters", MIN_LENGTH);

    public void apply(String pass) throws PolicyViolationException {

        if (pass == null || pass.length() < MIN_LENGTH) {
            throw new PolicyViolationException(POLICY_MESSAGE);
        }

//        Matcher charMatcher = charPattern.matcher(pass);
//        Matcher numMatcher = numPattern.matcher(pass);
//        Matcher specMatcher = specPattern.matcher(pass);

//        if (!charMatcher.matches() || !numMatcher.matches() || specMatcher.matches() || pass.length() < MIN_LENGTH) {
//
//        }
    }

}
