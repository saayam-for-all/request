package org.sfa.request.notification;

import org.junit.jupiter.api.Test;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.SentimentCode;
import org.sfa.request.service.api.SentimentAssessment;
import org.sfa.request.service.impl.LexiconSentimentAnalysisService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage 1 of the notification flow: only a clean request may be routed onwards.
 */
class LexiconSentimentAnalysisServiceTest {

    /** Built-in lexicons: the constructor falls back when a list is empty. */
    private final LexiconSentimentAnalysisService service =
            new LexiconSentimentAnalysisService(List.of(), List.of(), List.of());

    private static Request request(String subject, String description) {
        Request request = new Request();
        request.setRequestId("REQ-1");
        request.setRequestSubject(subject);
        request.setRequestDescription(description);
        return request;
    }

    @Test
    void cleanRequestIsGoodAndNotifiable() {
        SentimentAssessment assessment =
                service.assess(request("Need a ride", "I need a lift to my clinic appointment"));

        assertEquals(SentimentCode.GOOD_REQUEST, assessment.code());
        assertTrue(assessment.notifiable());
        assertNull(assessment.matchedTerm());
    }

    @Test
    void foulLanguageIsClassifiedAndNotNotifiable() {
        SentimentAssessment assessment =
                service.assess(request("Help", "the last volunteer was an idiot"));

        assertEquals(SentimentCode.FOUL_LANGUAGE, assessment.code());
        assertFalse(assessment.notifiable());
        assertEquals("idiot", assessment.matchedTerm());
    }

    @Test
    void suicidalLanguageIsClassifiedAndNotNotifiable() {
        SentimentAssessment assessment =
                service.assess(request("Please help", "I want to die, nothing helps any more"));

        assertEquals(SentimentCode.DEPRESSIVE_OR_SUICIDAL, assessment.code());
        assertFalse(assessment.notifiable());
    }

    @Test
    void threateningLanguageIsClassifiedAndNotNotifiable() {
        SentimentAssessment assessment =
                service.assess(request("Help", "bring a gun when you come"));

        assertEquals(SentimentCode.THREATENING, assessment.code());
        assertFalse(assessment.notifiable());
    }

    /** The most severe lexicon wins, not the first one to appear in the text. */
    @Test
    void mostSevereClassificationWins() {
        SentimentAssessment assessment =
                service.assess(request("idiot", "you are an idiot and I will shoot you"));

        assertEquals(SentimentCode.THREATENING, assessment.code());
    }

    @Test
    void depressiveOutranksFoul() {
        SentimentAssessment assessment =
                service.assess(request("Help", "this stupid situation, I am suicidal"));

        assertEquals(SentimentCode.DEPRESSIVE_OR_SUICIDAL, assessment.code());
    }

    /** Whole-word matching: a lexicon term inside a longer word must not trip. */
    @Test
    void substringInsideALongerWordDoesNotMatch() {
        SentimentAssessment assessment =
                service.assess(request("Gardening", "please help me cut the grasses and shrubs"));

        assertEquals(SentimentCode.GOOD_REQUEST, assessment.code(),
                "'grasses' contains 'gras' but must not match the term 'gun' or any other");
    }

    @Test
    void classificationIsCaseInsensitive() {
        assertEquals(SentimentCode.THREATENING, service.assess(request("HELP", "I WILL STAB HIM")).code());
    }

    @Test
    void accentedTextIsFolded() {
        assertEquals(SentimentCode.FOUL_LANGUAGE, service.assess(request("Help", "what an ídiot")).code());
    }

    @Test
    void subjectAloneCanTripTheClassifier() {
        assertEquals(SentimentCode.THREATENING, service.assess(request("bomb", "nothing here")).code());
    }

    @Test
    void nullAndBlankTextAreTreatedAsGood() {
        assertEquals(SentimentCode.GOOD_REQUEST, service.assess(request(null, null)).code());
        assertEquals(SentimentCode.GOOD_REQUEST, service.assess(request("  ", "")).code());
    }

    /** Operations can retune a lexicon from configuration without a code change. */
    @Test
    void configuredLexiconOverridesTheDefault() {
        LexiconSentimentAnalysisService configured = new LexiconSentimentAnalysisService(
                List.of("trebuchet"), List.of(), List.of());

        assertEquals(SentimentCode.THREATENING,
                configured.assess(request("Help", "I have a trebuchet")).code());
        assertEquals(SentimentCode.GOOD_REQUEST,
                configured.assess(request("Help", "bring a gun")).code(),
                "the configured list replaces the default rather than adding to it");
    }

    /** The seeded sentiment_codes contract: 0 good, 1 foul, 2 depressive, 3 threatening. */
    @Test
    void codesMatchTheSeededLookupTable() {
        assertEquals(0, SentimentCode.GOOD_REQUEST.getCode());
        assertEquals(1, SentimentCode.FOUL_LANGUAGE.getCode());
        assertEquals(2, SentimentCode.DEPRESSIVE_OR_SUICIDAL.getCode());
        assertEquals(3, SentimentCode.THREATENING.getCode());

        assertTrue(SentimentCode.GOOD_REQUEST.isNotifiable());
        assertFalse(SentimentCode.FOUL_LANGUAGE.isNotifiable());
        assertFalse(SentimentCode.DEPRESSIVE_OR_SUICIDAL.isNotifiable());
        assertFalse(SentimentCode.THREATENING.isNotifiable());

        assertEquals(SentimentCode.THREATENING, SentimentCode.fromCode(3).orElseThrow());
        assertTrue(SentimentCode.fromCode(99).isEmpty());
    }
}
