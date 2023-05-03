package com.networkchat.client;

import java.util.Date;

public class User {
    private String username;
    private String email;
    private String password;

    private byte[] publicKey;

    private byte[] salt;

    private Date timeStamp;
    private boolean enabled;

    public User(String username, String email, String password, Date timeStamp) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.timeStamp = timeStamp;
        this.enabled = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
