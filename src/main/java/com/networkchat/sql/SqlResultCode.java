package com.networkchat.sql;

public enum SqlResultCode {
    SUCCESS, REPEATED_EMAIL, EXISTING_USERNAME, NOT_EXISTING_USERNAME, WRONG_CODE, CORRECT_CODE, ALLOW_LOGIN, NOT_CONFIRMED,
    IS_CONFIRMED, ACCESS_DENIED
}
