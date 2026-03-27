package org.sfa.request.repository;

import org.sfa.request.model.entity.ReqAddInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReqAddInfoRepository extends JpaRepository<ReqAddInfo, Integer> {
}