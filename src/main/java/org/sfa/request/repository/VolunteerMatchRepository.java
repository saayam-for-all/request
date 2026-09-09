package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Finds volunteers whose registered skills cover a help request's category.
 *
 * <p>The join is meaningful because {@code user_skills.cat_id} is a foreign key to
 * {@code help_categories.cat_id} — a volunteer's "skills" are literally help-category
 * IDs. Presence of a row in {@code volunteer_details} is what makes a user a volunteer.
 *
 * <p>Deliberately does NOT filter on {@code users.user_status_id}: the
 * {@code user_status} lookup table has no committed seed data, so there is no
 * verifiable "active" value to filter on. Add that filter once the DB team seeds it.
 */
@Repository
public interface VolunteerMatchRepository extends JpaRepository<Request, String> {

    @Query(value = """
            SELECT u.user_id              AS "userId",
                   u.primary_email_address AS "emailAddress",
                   u.primary_phone_number  AS "phoneNumber"
            FROM virginia_dev_saayam_rdbms.user_skills us
            JOIN virginia_dev_saayam_rdbms.users u
                   ON u.user_id = us.user_id
            JOIN virginia_dev_saayam_rdbms.volunteer_details vd
                   ON vd.user_id = us.user_id
            WHERE us.cat_id = :catId
              AND u.user_id <> :requesterId
            ORDER BY u.user_id
            """, nativeQuery = true)
    List<MatchedVolunteerRow> findVolunteersByCategory(@Param("catId") String catId,
                                                       @Param("requesterId") String requesterId);
}
