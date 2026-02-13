package org.sfa.request.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "help_categories_map", schema = "virginia_dev_saayam_rdbms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelpCategoryMap {

    @Id
    @Column(name = "child_id")
    private String childId;

    @Column(name = "parent_id")
    private String parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "cat_id", insertable = false, updatable = false)
    private HelpCategory parentCategory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", referencedColumnName = "cat_id", insertable = false, updatable = false)
    private HelpCategory childCategory;
}
