package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.sfa.request.repository.projection.UserContactRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Read-only contact lookup on the users table, which this service does not own.
 *
 * <p>Uses the same columns as {@link VolunteerMatchRepository}. Extends the marker
 * {@link Repository} interface (not JpaRepository) so it exposes no CRUD methods;
 * {@code Request} is only the required domain type.</p>
 */
@org.springframework.stereotype.Repository
public interface UserContactRepository extends Repository<Request, String> {

    @Query(value = """
            SELECT u.user_id               AS "userId",
                   u.primary_email_address AS "emailAddress",
                   u.primary_phone_number  AS "phoneNumber"
            FROM virginia_dev_saayam_rdbms.users u
            WHERE u.user_id = :userId
            """, nativeQuery = true)
    Optional<UserContactRow> findContactByUserId(@Param("userId") String userId);
}
