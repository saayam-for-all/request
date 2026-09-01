package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "req_add_info", schema = "virginia_dev_saayam_rdbms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqAddInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id")
    private Integer infoId;

    @Column(name = "req_id", nullable = false)
    private String reqId;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @Column(name = "item_id")
    private String itemId;        // NULL for value-type fields

    @Column(name = "field_value")
    private String fieldValue;    // NULL for list-type fields
}