package org.sfa.request.service.api;

import org.sfa.request.model.enums.SentimentCode;

/**
 * Outcome of classifying a help request's free text.
 *
 * @param code         the classification, mirroring the {@code sentiment_codes} lookup
 * @param matchedTerm  the lexicon term that drove a negative classification, or
 *                     {@code null} for {@link SentimentCode#GOOD_REQUEST}. Useful for
 *                     tests and moderator tooling. Callers must not write it to logs
 *                     alongside the request text (NFR-02, privacy).
 */
public record SentimentAssessment(SentimentCode code, String matchedTerm) {

    public static SentimentAssessment good() {
        return new SentimentAssessment(SentimentCode.GOOD_REQUEST, null);
    }

    /** Whether this request may be routed to volunteers and notified. */
    public boolean notifiable() {
        return code.isNotifiable();
    }
}
