package com.networkchat.packets.server;

public enum ServerResponse {
    EXISTING_USERNAME, REPEATED_EMAIL, SUCCESSFUL_REGISTRATION, LOGIN_DENIED, LOGIN_ALLOWED, USER_NOT_CONFIRMED, OPEN_RSA_KEY,
    INVALID_CODE, VALID_CODE, NEW_USER_CONNECTED, MESSAGE, DISCONNECTED, ALREADY_LOGGED_IN
}
