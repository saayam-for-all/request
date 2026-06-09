package org.sfa.request.repository;

import org.sfa.request.model.entity.ReqAddInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public interface ReqAddInfoRepository extends JpaRepository<ReqAddInfo, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ReqAddInfo r WHERE r.reqId = :reqId")
    void deleteByReqId(@Param("reqId") String reqId);
}