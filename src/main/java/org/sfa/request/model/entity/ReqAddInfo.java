package org.sfa.request.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String requestId;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "field_value")
    private String fieldValue;
}
