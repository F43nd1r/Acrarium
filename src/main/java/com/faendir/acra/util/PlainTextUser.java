package com.faendir.acra.util;

import com.faendir.acra.model.User;

import java.util.Collection;

/**
 * @author lukas
 * @since 21.05.18
 */
public class PlainTextUser extends User {
    private final String plaintextPassword;

    public PlainTextUser(String username, String plaintextPassword, String encodedPassword, Collection<Role> roles) {
        super(username, encodedPassword, roles);
        this.plaintextPassword = plaintextPassword;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }
}
