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
 * @author Fan Peng
 * Create 2024/6/13 22:38
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "request",
        uniqueConstraints = {
                @UniqueConstraint(name = "req_id_unique", columnNames = "req_id")
        }
)
public class Request implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "req_id", updatable = false, columnDefinition = "VARCHAR(255)")
    private String requestId;

    @Column(name = "req_user_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String requesterId;

    @ManyToOne
    @JoinColumn(
            name = "req_status_id",
            nullable = false,
            referencedColumnName = "req_status_id",
            foreignKey = @ForeignKey(name = "fk_req_status_id")
    )
    private RequestStatus requestStatus;


    @ManyToOne
    @JoinColumn(
            name = "req_priority_id",
            nullable = false,
            referencedColumnName = "req_priority_id",
            foreignKey = @ForeignKey(name = "fk_req_priority_id")
    )
    private RequestPriority requestPriority;

    @ManyToOne
    @JoinColumn(
            name = "req_type_id",
            nullable = false,
            referencedColumnName = "req_type_id",
            foreignKey = @ForeignKey(name = "fk_req_type_id")
    )
    private RequestType requestType;
    
	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn( name = "req_cat_id", nullable = false, insertable = false,
	 * updatable = false, referencedColumnName = "request_category_id", foreignKey
	 * = @ForeignKey(name = "fk_req_cat_id") ) private RequestCategory
	 * requestCategory;
	 * 
	 */ 
    @ManyToOne
    @JoinColumn(
            name = "req_cat_id",
            nullable = false,
            referencedColumnName = "cat_id",
            foreignKey = @ForeignKey(name = "fk_cat_id")
    )
    private HelpCategory helpCategory;

    

    @ManyToOne
    @JoinColumn(
            name = "req_for_id",
            nullable = false,
            referencedColumnName = "req_for_id",
            foreignKey = @ForeignKey(name = "fk_req_for_id")
    )
    private RequestFor requestFor;

    
//    @Column(name = "city_name", columnDefinition = "VARCHAR(255)")
    @Transient
    private String city;

//    @Column(name = "zip_code", columnDefinition = "VARCHAR(20)")
    @Transient
    private String zipCode;

    @Column(name = "req_desc", nullable = false, columnDefinition = "VARCHAR(255)")
    private String requestDescription;

    @Column(name = "audio_req_desc", columnDefinition = "VARCHAR(255)")
    private String audioRequestDescription;
    

    @Column(name = "submission_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime submittedAt;

//    @Column(name = "lead_volunteer_user_id")
    private Integer leadVolunteerUserId;

    @Column(name = "serviced_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime servicedAt;

    @Column(name = "last_update_date", columnDefinition = "TIMESTAMP")
    private ZonedDateTime lastUpdatedAt;
    
    @ManyToOne
    @JoinColumn(
            name = "req_islead_id",
            nullable = false,
            referencedColumnName = "req_islead_id",
            foreignKey = @ForeignKey(name = "fk_req_islead_id")
    )
    private RequestIsLeadVol  requestIsLeadVol;
	  
//	  @Column(name = "req_loc", nullable = false, length = 125) 
//	  private String requestLoc;
	  
	  @Column(name = "req_subj", nullable = false, length = 125) 
	  private String requestSubject; 
	  
	 
  
}