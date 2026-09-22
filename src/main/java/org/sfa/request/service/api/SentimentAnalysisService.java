package org.sfa.request.service.api;

import org.sfa.request.model.entity.Request;

/**
 * Classifies a help request's free text before it is routed to volunteers.
 *
 * <p>This is the first stage of the notification flow: sentiment analysis, then
 * in-person/remote volunteer matching, then notification dispatch. A request that
 * is not {@link SentimentAssessment#notifiable()} never reaches the later stages.
 *
 * <p>The default implementation is deterministic and lexicon-driven. It is
 * intentionally behind this interface so the model-backed classifier tracked by
 * saayam-for-all/ai#7 can replace it without touching the routing logic.
 */
public interface SentimentAnalysisService {

    SentimentAssessment assess(Request request);
}
