package com.networkchat.security.idea;

import java.io.Serializable;

public class IdeaKeys implements Serializable {
    private String encryptKey;
    private String decryptKey;

    public IdeaKeys(String encryptKey, String decryptKey) {
        this.encryptKey = encryptKey;
        this.decryptKey = decryptKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(String decryptKey) {
        this.decryptKey = decryptKey;
    }
}
