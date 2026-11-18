package org.sfa.request.repository.projection;

/**
 * A user's contact details from the users table.
 */
public interface UserContactRow {
    String getUserId();

    String getEmailAddress();

    String getPhoneNumber();
}
