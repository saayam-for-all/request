package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", schema = "virginia_dev_saayam_rdbms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "time_zone")
    private String timeZone;
}