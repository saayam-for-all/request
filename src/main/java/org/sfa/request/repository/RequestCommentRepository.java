package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    List<RequestComment> findByRequestId(String requestId);
}