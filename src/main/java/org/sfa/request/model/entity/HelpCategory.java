package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "help_categories", schema = "virginia_dev_saayam_rdbms")

public class HelpCategory {
    @Id
    @Column(name = "cat_id", nullable = false, columnDefinition = "VARCHAR(255)")
    private String catId;

    @Column(name = "cat_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String catName;

    @Column(name = "cat_desc", length = 2000)
    private String catDesc;
}