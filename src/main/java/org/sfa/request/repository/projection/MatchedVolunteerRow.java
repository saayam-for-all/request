package org.sfa.request.repository.projection;

/**
 * Projection for a volunteer matched to a help request by skill category.
 */
public interface MatchedVolunteerRow {
    String getUserId();

    String getEmailAddress();

    String getPhoneNumber();
}
