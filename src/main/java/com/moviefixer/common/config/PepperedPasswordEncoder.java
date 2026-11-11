package com.moviefixer.common.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * Custom PasswordEncoder that applies a secret application-level System Salt ("Pepper")
 * prior to executing BCrypt's per-password random salting.
 *
 * Defense-in-depth: Even in the event of a database compromise, attacker hashes cannot
 * be brute-forced or cracked without this server-side secret system salt.
 */
public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final String pepper;

    public PepperedPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate PasswordEncoder must not be null");
        this.pepper = pepper != null ? pepper : "";
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return delegate.encode(rawPassword + pepper);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            return false;
        }
        return delegate.matches(rawPassword + pepper, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }
}
