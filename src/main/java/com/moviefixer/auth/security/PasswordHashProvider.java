package com.moviefixer.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashProvider {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    private final SecretKeySpec pepperKey;

    public PasswordHashProvider(
            @Value("${app.security.password-pepper}")
            String pepper) {

        if (pepper == null || pepper.isEmpty()) {
            throw new IllegalStateException(
                    "Password pepper is not configured");
        }

        this.pepperKey = new SecretKeySpec(
                pepper.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
    }

    public String hash(String password) {
        return encoder.encode(pepperPassword(password));
    }

    public boolean matches(String password, String storedHash) {
        return encoder.matches(
                pepperPassword(password), storedHash);
    }

    private String pepperPassword(String password) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pepperKey);

            byte[] result = mac.doFinal(
                    password.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(result);

        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Password preprocessing failed", e);
        }
    }
}