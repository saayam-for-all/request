package org.sfa.request.repository;

import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReqAddInfoMetadataRepository extends JpaRepository<ReqAddInfoMetadata, String> {
    List<ReqAddInfoMetadata> findByCatId(String catId);
    List<ReqAddInfoMetadata> findByCatIdAndStatus(String catId, String status);

}
