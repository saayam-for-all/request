package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    List<RequestComment> findByRequestIdAndIsDeletedFalseOrderByCreatedAtAscIdAsc(
            String requestId
    );

    Optional<RequestComment> findByIdAndIsDeletedFalse(Long id);
}