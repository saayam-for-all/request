package org.sfa.request.service.impl;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generates numeric one-time codes (Part 3 of the workflow, issue #95).
 */
@Component
public class OtpCodeGenerator {

    /** Digits per code. 6 digits with 3 attempts gives about a 3-in-a-million guess chance. */
    public static final int CODE_LENGTH = 6;

    private static final int BOUND = (int) Math.pow(10, CODE_LENGTH);

    private final Random random;

    public OtpCodeGenerator() {
        this(new SecureRandom());
    }

    /** Used by tests to make generation deterministic. */
    OtpCodeGenerator(Random random) {
        this.random = random;
    }

    /** A single zero-padded code, e.g. {@code "004821"}. */
    public String generate() {
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt(BOUND));
    }

    /**
     * Two codes that are guaranteed to differ, so neither party can verify by simply
     * entering the code they were sent themselves.
     */
    public String[] generateDistinctPair() {
        String first = generate();
        String second = generate();
        while (second.equals(first)) {
            second = generate();
        }
        return new String[]{first, second};
    }
}
