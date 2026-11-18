package org.sfa.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sfa.request.model.enums.OtpParty;

import static org.junit.jupiter.api.Assertions.*;

class OtpCodeHasherTest {

    private static final String RECORD_ID = "0b6c1f1e-1111-2222-3333-444455556666";
    private final OtpCodeHasher hasher = new OtpCodeHasher("test-secret");

    @Test
    void hashIsDeterministicHexAndDoesNotContainTheCode() {
        String first = hasher.hash(RECORD_ID, OtpParty.REQUESTER, "123456");
        String second = hasher.hash(RECORD_ID, OtpParty.REQUESTER, "123456");

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertTrue(first.matches("[0-9a-f]{64}"));
        assertFalse(first.contains("123456"));
    }

    @Test
    void matchesTheRightCode() {
        String stored = hasher.hash(RECORD_ID, OtpParty.REQUESTER, "123456");

        assertTrue(hasher.matches(RECORD_ID, OtpParty.REQUESTER, "123456", stored));
        assertTrue(hasher.matches(RECORD_ID, OtpParty.REQUESTER, " 123456 ", stored), "surrounding spaces are ignored");
        assertFalse(hasher.matches(RECORD_ID, OtpParty.REQUESTER, "123457", stored));
        assertFalse(hasher.matches(RECORD_ID, OtpParty.REQUESTER, null, stored));
        assertFalse(hasher.matches(RECORD_ID, OtpParty.REQUESTER, "123456", null));
    }

    @Test
    void hashIsBoundToTheRecordAndTheCodeOwner() {
        String stored = hasher.hash(RECORD_ID, OtpParty.REQUESTER, "123456");

        assertFalse(hasher.matches("another-record", OtpParty.REQUESTER, "123456", stored));
        assertFalse(hasher.matches(RECORD_ID, OtpParty.LEAD_VOLUNTEER, "123456", stored));
    }

    @Test
    void differentSecretsGiveDifferentHashes() {
        String a = new OtpCodeHasher("secret-a").hash(RECORD_ID, OtpParty.REQUESTER, "123456");
        String b = new OtpCodeHasher("secret-b").hash(RECORD_ID, OtpParty.REQUESTER, "123456");

        assertNotEquals(a, b);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void missingSecretFailsOnUse(String secret) {
        OtpCodeHasher unconfigured = new OtpCodeHasher(secret);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> unconfigured.hash(RECORD_ID, OtpParty.REQUESTER, "123456"));
        assertTrue(e.getMessage().contains("OTP_HMAC_SECRET"));
    }
}
