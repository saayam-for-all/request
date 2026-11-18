package org.sfa.request.service.impl;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class OtpCodeGeneratorTest {

    @RepeatedTest(50)
    void generatesSixDigitNumericCodes() {
        String code = new OtpCodeGenerator().generate();

        assertEquals(OtpCodeGenerator.CODE_LENGTH, code.length());
        assertTrue(code.chars().allMatch(Character::isDigit), code);
    }

    @Test
    void padsSmallValuesWithLeadingZeros() {
        OtpCodeGenerator generator = new OtpCodeGenerator(sequence(42));

        assertEquals("000042", generator.generate());
    }

    @Test
    void pairIsAlwaysDistinct() {
        // The second draw repeats the first, so the generator must draw again.
        OtpCodeGenerator generator = new OtpCodeGenerator(sequence(123456, 123456, 654321));

        String[] pair = generator.generateDistinctPair();

        assertEquals("123456", pair[0]);
        assertEquals("654321", pair[1]);
    }

    @RepeatedTest(50)
    void randomPairsDiffer() {
        String[] pair = new OtpCodeGenerator().generateDistinctPair();

        assertNotEquals(pair[0], pair[1]);
    }

    /** A Random whose nextInt(bound) returns the given values in order. */
    private static Random sequence(Integer... values) {
        Deque<Integer> queue = new ArrayDeque<>(List.of(values));
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return queue.removeFirst();
            }
        };
    }
}
