package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "req_add_info_metadata", schema = "virginia_dev_saayam_rdbms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqAddInfoMetadata {

    @Id
    @Column(name = "field_id")
    private String fieldId;

    @Column(name = "field_name_key")
    private String fieldNameKey;

    @Column(name = "field_type")
    private String fieldType;

    @Column(name = "status")
    private String status;

    @Column(name = "cat_id")
    private String catId;

    @OneToMany(mappedBy = "metadataField", fetch = FetchType.LAZY)
    private List<ListItemMetadata> listItems;
}
