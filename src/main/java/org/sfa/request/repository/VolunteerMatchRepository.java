package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.sfa.request.repository.projection.MatchedVolunteerRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Finds the volunteers who should be told about a help request.
 *
 * <p>Two strategies, picked by request type (see {@code VolunteerMatchServiceImpl}):
 * skill matching for remote requests and proximity matching for in-person ones.
 *
 * <p>Neither query filters on {@code users.user_status_id}: the {@code user_status}
 * lookup table has no committed seed data, so there is no verifiable "active" value
 * to filter on. Add that filter once the DB team seeds it.
 */
@Repository
public interface VolunteerMatchRepository extends JpaRepository<Request, String> {

    /**
     * Skill matching, used for REMOTE requests.
     *
     * <p>The join is meaningful because {@code user_skills.cat_id} is a foreign key to
     * {@code help_categories.cat_id} — a volunteer's "skills" are literally help-category
     * IDs. Presence of a row in {@code volunteer_details} is what makes a user a volunteer.
     */
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

    /**
     * Proximity matching, used for IN_PERSON requests.
     *
     * <p>Anchored on the <em>requester's</em> current location, because
     * {@code request.req_loc} is free text with no coordinates. Candidates come from
     * {@code volunteer_locations}, whose {@code curr_loc} is a
     * {@code geography(Point, 4326)} with a GIST index, so {@code ST_DWithin} is an
     * index-assisted range scan rather than a full-table distance computation.
     *
     * <p>This mirrors the query the spatial microservice runs in
     * {@code sql/find_nearest_volunteers.sql}. It is issued in-process for the same
     * reason the skill query is: the request service already reads volunteer tables
     * directly and has no HTTP client. If the spatial service later becomes the sole
     * owner of these tables, this method is the single seam to replace.
     *
     * @param requesterId  the requester, used both as the anchor and to exclude self
     * @param radiusMeters outer search radius, in metres
     * @param maxResults   cap on how many volunteers are notified for one request
     */
    @Query(value = """
            SELECT u.user_id               AS "userId",
                   u.primary_email_address AS "emailAddress",
                   u.primary_phone_number  AS "phoneNumber"
            FROM virginia_dev_saayam_rdbms.user_locations rl
            JOIN virginia_dev_saayam_rdbms.volunteer_locations vl
                   ON ST_DWithin(vl.curr_loc, rl.curr_loc, :radiusMeters)
            JOIN virginia_dev_saayam_rdbms.users u
                   ON u.user_id = vl.user_id
            JOIN virginia_dev_saayam_rdbms.volunteer_details vd
                   ON vd.user_id = vl.user_id
            WHERE rl.user_id = :requesterId
              AND rl.curr_loc IS NOT NULL
              AND vl.curr_loc IS NOT NULL
              AND u.user_id <> :requesterId
            ORDER BY ST_Distance(vl.curr_loc, rl.curr_loc), u.user_id
            LIMIT :maxResults
            """, nativeQuery = true)
    List<MatchedVolunteerRow> findVolunteersNearRequester(@Param("requesterId") String requesterId,
                                                          @Param("radiusMeters") double radiusMeters,
                                                          @Param("maxResults") int maxResults);
}
