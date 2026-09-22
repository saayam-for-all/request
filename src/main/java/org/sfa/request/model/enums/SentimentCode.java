package org.sfa.request.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Sentiment classification of an incoming help request.
 *
 * <p>The codes and labels mirror the seeded {@code sentiment_codes} lookup table
 * (see {@code database/ddl/Tables/Scripts/*_saayam_rdbms.sql}), which is the
 * contract the {@code fraud_requests} table's {@code ref_code} column references.
 * Keep the numeric codes in step with that seed data.
 *
 * <p>Only {@link #GOOD_REQUEST} is notifiable. A request carrying foul,
 * depressive/suicidal or threatening language is never routed to volunteers.
 */
@Getter
public enum SentimentCode {

    /** Request is clean, no harmful or negative content detected. */
    GOOD_REQUEST(0, "Good Request", true),

    /** Request contains offensive or foul language. */
    FOUL_LANGUAGE(1, "Foul Language", false),

    /** Request contains depressive or suicidal language. */
    DEPRESSIVE_OR_SUICIDAL(2, "Depressive or Suicidal", false),

    /** Request contains threatening language or references to weapons. */
    THREATENING(3, "Threatening", false);

    private final int code;
    private final String label;

    /** Whether a request with this sentiment may be routed to volunteers. */
    private final boolean notifiable;

    SentimentCode(int code, String label, boolean notifiable) {
        this.code = code;
        this.label = label;
        this.notifiable = notifiable;
    }

    public static Optional<SentimentCode> fromCode(int code) {
        return Arrays.stream(values())
                .filter(sentiment -> sentiment.code == code)
                .findFirst();
    }
}
