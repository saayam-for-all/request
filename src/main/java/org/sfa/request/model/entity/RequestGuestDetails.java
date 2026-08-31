package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "request_other_details", schema = "virginia_dev_saayam_rdbms")
public class RequestGuestDetails {

    @Id
    @Column(name = "req_id", nullable = false)
    private String requestId; // FK and PK

    @Column(name = "req_fname", nullable = false, length = 100)
    private String reqFname;

    @Column(name = "req_lname", nullable = false, length = 100)
    private String reqLname;

    @Column(name = "req_email", length = 100)
    private String reqEmail;

    @Column(name = "req_phone", nullable = false, length = 20)
    private String reqPhone;

    @Column(name = "req_age")
    private Integer reqAge;

    @Column(name = "req_gender", length = 50)
    private String reqGender;

    @Column(name = "req_pref_lang", length = 50)
    private String reqPrefLang;

    @Column(name = "last_updated_at")
    private ZonedDateTime lastUpdatedAt;

    @Column(name = "user_id")
    private String userId;
}
