package org.sfa.request.repository;

import org.sfa.request.model.entity.ListItemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListItemMetadataRepository extends JpaRepository<ListItemMetadata, String> {
    List<ListItemMetadata> findByFieldId(String fieldId);
    List<ListItemMetadata> findByFieldIdIn(List<String> fieldIds);
}
