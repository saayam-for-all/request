package org.sfa.request.model.entity;

import lombok.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * ClassName: Request
 * Package: org.sfa.request.model.entity
 * Description:
 *
 * @author Shariq
 * Create 2025/11/1 22:38
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "requestStatus",
        "requestPriority",
        "requestType",
        "helpCategory",
        "requestFor",
        "isLeadVolunteer"
})
@Entity
@Table(
        name = "request",
        schema = "virginia_dev_saayam_rdbms",
        uniqueConstraints = {
                @UniqueConstraint(name = "request_id_unique", columnNames = "req_id")
        }
)
public class Request implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_id", updatable = false, nullable = false)
    private String requestId;

    @Column(name = "req_user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String requesterId;

    @ManyToOne
    @JoinColumn(
            name = "req_status_id",
            nullable = false,
            referencedColumnName = "req_status_id",
            foreignKey = @ForeignKey(name = "fk_request_status_id")
    )
    private RequestStatus requestStatus;

    @ManyToOne
    @JoinColumn(
            name = "req_priority_id",
            nullable = false,
            referencedColumnName = "req_priority_id",
            foreignKey = @ForeignKey(name = "fk_request_priority_id")
    )
    private RequestPriority requestPriority;

    @ManyToOne
    @JoinColumn(
            name = "req_type_id",
            nullable = false,
            referencedColumnName = "req_type_id",
            foreignKey = @ForeignKey(name = "fk_request_type_id")
    )
    private RequestType requestType;

    @ManyToOne
    @JoinColumn(
            name = "req_cat_id",
            nullable = false,
            referencedColumnName = "cat_id",
            foreignKey = @ForeignKey(name = "fk_request_category_id")
    )
    private HelpCategory helpCategory;

    @ManyToOne
    @JoinColumn(
            name = "req_for_id",
            nullable = false,
            referencedColumnName = "req_for_id",
            foreignKey = @ForeignKey(name = "fk_request_for_id")
    )
    private RequestFor requestFor;

    @Column(name = "req_loc", columnDefinition = "VARCHAR(125)")
    private String requestLocation;

    @Column(name = "iscalamity")
    private Boolean isCalamity;

    @Column(name = "req_subj", nullable = false, columnDefinition = "VARCHAR(125)")
    private String requestSubject;

    @Column(name = "req_desc", nullable = false, columnDefinition = "VARCHAR(255)")
    private String requestDescription;

    @Column(name = "req_doc_link", columnDefinition = "TEXT")
    private String requestDocumentLink;

    @Column(name = "audio_req_desc", columnDefinition = "VARCHAR(255)")
    private String audioRequestDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "req_islead_id",
            nullable = false,
            referencedColumnName = "req_islead_id",
            foreignKey = @ForeignKey(name = "fk_request_islead_id")
    )
    private RequestIsLeadVolunteer isLeadVolunteer;

    @Column(name = "submission_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime submittedAt;

    @Column(name = "serviced_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime servicedAt;

    @Column(name = "last_update_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdatedAt;
}
