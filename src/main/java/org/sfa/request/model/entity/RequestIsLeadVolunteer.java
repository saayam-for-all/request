package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Data
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "request_isleadvol", schema = "virginia_dev_saayam_rdbms")
public class RequestIsLeadVolunteer {

    @Id
    @Column(name = "req_islead_id")
    private Integer reqIsleadId;

    @Column(name = "req_islead")
    private String reqIslead;

    @Column(name = "req_islead_desc")
    private String reqIsleadDesc;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

}