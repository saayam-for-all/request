package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ClassName: RequestRepository
 * Package: org.sfa.request.repository
 * Description:
 *
 * @author Fan Peng
 * Create 2024/6/14 23:20
 * @version 1.0
 */

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

    @Query("SELECT r FROM Request r WHERE r.requesterId = :requesterId AND r.requestStatus.requestStatusId != :deletedStatusId")
    Page<Request> findAllActiveByRequesterId(
            @Param("requesterId") String requesterId,
            @Param("deletedStatusId") int deletedStatusId,
            Pageable pageable
    );

    @Query("SELECT r FROM Request r WHERE r.requestId = :requestId AND r.requesterId = :requesterId AND r.requestStatus.requestStatusId != :deletedStatusId")
    Optional<Request> findActiveByRequestIdAndRequesterId(
            @Param("requestId") String requestId,
            @Param("requesterId") String requesterId,
            @Param("deletedStatusId") int deletedStatusId
    );

    @Query("SELECT r FROM Request r WHERE r.requestId = :requestId AND r.requesterId = :requesterId")
    Optional<Request> findByRequestIdAndRequesterIdIncludingDeleted(
            @Param("requestId") String requestId,
            @Param("requesterId") String requesterId
    );
}


