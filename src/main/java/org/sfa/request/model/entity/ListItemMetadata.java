package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "list_item_metadata", schema = "virginia_dev_saayam_rdbms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListItemMetadata {

    @Id
    @Column(name = "item_id")
    private String itemId;

    @Column(name = "field_id", insertable = false, updatable = false)
    private String fieldId;

    @Column(name = "item_value")
    private String itemValue;

    @Column(name = "item_type")
    private String itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", referencedColumnName = "field_id", insertable = false, updatable = false)
    private ReqAddInfoMetadata metadataField;
}
