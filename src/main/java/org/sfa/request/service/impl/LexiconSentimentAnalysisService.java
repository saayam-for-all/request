package org.sfa.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sfa.request.model.entity.Request;
import org.sfa.request.model.enums.SentimentCode;
import org.sfa.request.service.api.SentimentAnalysisService;
import org.sfa.request.service.api.SentimentAssessment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic, lexicon-driven sentiment classifier.
 *
 * <p>This is the MVP stand-in for the model-backed classifier tracked by
 * saayam-for-all/ai#7. It is deliberately simple and predictable so the routing
 * behaviour around it can be tested and reasoned about today. When that classifier
 * lands it can take over by implementing {@link SentimentAnalysisService} and
 * annotating itself {@code @Primary}; nothing in the routing logic changes.
 *
 * <h2>How it classifies</h2>
 * The request subject and description are folded to lower case, stripped of
 * accents, and scanned for whole-word lexicon hits. The <em>most severe</em>
 * match wins, so a message containing both an insult and a threat is classified
 * {@link SentimentCode#THREATENING} rather than {@link SentimentCode#FOUL_LANGUAGE}.
 *
 * <h2>Lexicons</h2>
 * Each lexicon is overridable from configuration, so operations can tune the
 * word lists (and localise them) without a code change. The built-in defaults are
 * a deliberately small, conservative seed rather than an exhaustive list — this
 * classifier is a gate that fails safe, not a content-moderation product.
 *
 * <p>Matching is whole-word, so "classic" does not trip on "ass" and "grasses"
 * does not trip on "gun".
 */
@Slf4j
@Service
public class LexiconSentimentAnalysisService implements SentimentAnalysisService {

    /**
     * Severity order: the first entry whose lexicon matches wins. Most severe first.
     */
    private final Map<SentimentCode, Pattern> lexicons = new LinkedHashMap<>();

    public LexiconSentimentAnalysisService(
            @Value("${saayam.sentiment.lexicon.threatening:}") List<String> threatening,
            @Value("${saayam.sentiment.lexicon.depressive:}") List<String> depressive,
            @Value("${saayam.sentiment.lexicon.foul:}") List<String> foul) {

        lexicons.put(SentimentCode.THREATENING,
                compile(orDefault(threatening, DEFAULT_THREATENING)));
        lexicons.put(SentimentCode.DEPRESSIVE_OR_SUICIDAL,
                compile(orDefault(depressive, DEFAULT_DEPRESSIVE)));
        lexicons.put(SentimentCode.FOUL_LANGUAGE,
                compile(orDefault(foul, DEFAULT_FOUL)));
    }

    /**
     * References to weapons or intent to harm another person.
     */
    private static final List<String> DEFAULT_THREATENING = List.of(
            "kill you", "kill him", "kill her", "kill them",
            "shoot", "stab", "bomb", "gun", "knife", "weapon",
            "hurt you", "burn your", "threat", "revenge");

    /**
     * Self-harm and crisis language. These requests need a human, not a volunteer
     * broadcast, which is why they are never notifiable.
     */
    private static final List<String> DEFAULT_DEPRESSIVE = List.of(
            "kill myself", "end my life", "suicide", "suicidal",
            "self harm", "self-harm", "want to die", "hopeless",
            "no reason to live", "worthless", "cant go on", "can't go on");

    /**
     * Abusive language aimed at a person. Kept small and clinical on purpose;
     * operations should supply the production list through configuration.
     */
    private static final List<String> DEFAULT_FOUL = List.of(
            "idiot", "stupid", "moron", "scum", "trash", "shut up");

    @Override
    public SentimentAssessment assess(Request request) {
        String text = normalise(
                join(request.getRequestSubject(), request.getRequestDescription()));

        if (text.isBlank()) {
            return SentimentAssessment.good();
        }

        for (Map.Entry<SentimentCode, Pattern> lexicon : lexicons.entrySet()) {
            Pattern pattern = lexicon.getValue();
            if (pattern == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // The matched term is a fixed lexicon entry, but it is still
                // request-derived, so it is carried on the result rather than logged.
                return new SentimentAssessment(lexicon.getKey(), matcher.group());
            }
        }

        return SentimentAssessment.good();
    }

    private static String join(String subject, String description) {
        return (subject == null ? "" : subject) + " " + (description == null ? "" : description);
    }

    /**
     * Lower-cases and strips accents so "IDIOT", "idiot" and "ídiot" all match.
     */
    private static String normalise(String text) {
        String folded = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return folded.toLowerCase();
    }

    private static List<String> orDefault(List<String> configured, List<String> fallback) {
        if (configured == null || configured.isEmpty()
                || configured.stream().allMatch(term -> term == null || term.isBlank())) {
            return fallback;
        }
        return configured;
    }

    /**
     * Builds one alternation with word boundaries on each side. Returns null for an
     * empty lexicon so that lexicon is skipped rather than matching everything.
     */
    private static Pattern compile(List<String> terms) {
        String alternation = terms.stream()
                .filter(term -> term != null && !term.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b)
                .orElse(null);

        if (alternation == null) {
            return null;
        }
        return Pattern.compile("\\b(?:" + alternation + ")\\b");
    }
}
