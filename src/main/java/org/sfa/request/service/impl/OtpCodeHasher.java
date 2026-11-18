package org.sfa.request.service.impl;

import org.sfa.request.model.enums.OtpParty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Hashes verification codes before they are stored (Part 3 of the workflow, issue #95).
 *
 * <p>Uses HMAC-SHA256 with a server-side secret rather than a plain hash: a 6-digit
 * code has only a million possible values, so a plain hash could be reversed by
 * trying them all. Each HMAC also binds the record ID and the code's owner, so a
 * hash cannot be reused across records or swapped between the two parties.</p>
 *
 * <p>The secret is read from {@code saayam.otp.hmac-secret} and checked on use, not at
 * startup, so a missing secret only affects OTP calls and not the rest of the service.</p>
 */
@Component
public class OtpCodeHasher {

    private static final String ALGORITHM = "HmacSHA256";

    private final String secret;

    public OtpCodeHasher(@Value("${saayam.otp.hmac-secret:}") String secret) {
        this.secret = secret;
    }

    /** Hex-encoded HMAC of the code, bound to the record and the code's owner. */
    public String hash(String otpVerificationId, OtpParty owner, String code) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(requireSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM));
            String message = otpVerificationId + ":" + owner.name() + ":" + code;
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to hash verification code", e);
        }
    }

    /** Constant-time comparison of a submitted code against a stored hash. */
    public boolean matches(String otpVerificationId, OtpParty owner, String submittedCode, String storedHash) {
        if (submittedCode == null || storedHash == null) {
            return false;
        }
        byte[] expected = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(otpVerificationId, owner, submittedCode.trim()).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private String requireSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("OTP HMAC secret is not configured. Set OTP_HMAC_SECRET.");
        }
        return secret;
    }
}
