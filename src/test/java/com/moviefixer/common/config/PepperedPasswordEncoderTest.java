package com.moviefixer.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PepperedPasswordEncoder Unit Tests")
class PepperedPasswordEncoderTest {

    private static final String PEPPER = "secret_system_pepper_salt_2026";
    private PepperedPasswordEncoder pepperedEncoder;
    private BCryptPasswordEncoder standardBCryptEncoder;

    @BeforeEach
    void setUp() {
        standardBCryptEncoder = new BCryptPasswordEncoder(4); // fast strength for unit test
        pepperedEncoder = new PepperedPasswordEncoder(standardBCryptEncoder, PEPPER);
    }

    @Test
    @DisplayName("encode() and matches() succeed with identical password and pepper")
    void encodeAndMatches_withValidPassword_returnsTrue() {
        String rawPassword = "StrongP@ssw0rd!2026";
        String encoded = pepperedEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(pepperedEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("matches() fails when supplied with an incorrect raw password")
    void matches_withIncorrectPassword_returnsFalse() {
        String rawPassword = "CorrectPassword123!";
        String encoded = pepperedEncoder.encode(rawPassword);

        assertFalse(pepperedEncoder.matches("WrongPassword123!", encoded));
    }

    @Test
    @DisplayName("matches() fails if verified with a different system pepper (Defense-in-depth verification)")
    void matches_withDifferentPepper_returnsFalse() {
        String rawPassword = "CustomerSecurePassword!";
        String encoded = pepperedEncoder.encode(rawPassword);

        PepperedPasswordEncoder wrongPepperEncoder = new PepperedPasswordEncoder(
                standardBCryptEncoder,
                "attacker_guessed_wrong_pepper"
        );

        assertFalse(wrongPepperEncoder.matches(rawPassword, encoded),
                "Hash generated with system pepper must NOT match when tested against a wrong pepper");
    }

    @Test
    @DisplayName("Standard BCrypt without system pepper fails to match peppered hash")
    void matches_withoutPepper_returnsFalse() {
        String rawPassword = "AdminSecurePassword!";
        String pepperedHash = pepperedEncoder.encode(rawPassword);

        assertFalse(standardBCryptEncoder.matches(rawPassword, pepperedHash),
                "Raw BCrypt matching without system pepper must fail");
    }

    @Test
    @DisplayName("matches() safely returns false when raw password is null")
    void matches_withNullRawPassword_returnsFalse() {
        String encoded = pepperedEncoder.encode("Password123!");
        assertFalse(pepperedEncoder.matches(null, encoded));
    }

    @Test
    @DisplayName("encode() throws IllegalArgumentException when raw password is null")
    void encode_withNullRawPassword_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> pepperedEncoder.encode(null));
    }
}
