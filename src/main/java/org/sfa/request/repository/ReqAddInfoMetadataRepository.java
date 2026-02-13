package org.sfa.request.repository;

import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.sfa.request.repository.projection.MetadataCatFieldItemRow;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReqAddInfoMetadataRepository extends JpaRepository<ReqAddInfoMetadata, String> {

    List<ReqAddInfoMetadata> findByCatId(String catId);
    List<ReqAddInfoMetadata> findByCatIdAndStatus(String catId, String status);

    @Query(value = """
        SELECT
          m.cat_id         AS catId,
          m.field_id       AS fieldId,
          m.field_name_key AS fieldNameKey,
          m.field_type     AS fieldType,
          m.status         AS status,
          i.item_id        AS itemId,
          i.item_value     AS itemValue,
          i.item_type      AS itemType
        FROM virginia_dev_saayam_rdbms.req_add_info_metadata m
        LEFT JOIN virginia_dev_saayam_rdbms.list_item_metadata i
          ON i.field_id = m.field_id
        WHERE m.status = 'active'
        ORDER BY m.cat_id, m.field_id, i.item_id
        """, nativeQuery = true)
    List<MetadataCatFieldItemRow> fetchAllMetadataWithItems();
}
