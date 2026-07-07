package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestPriorityRepository extends JpaRepository<RequestPriority, Integer> {
}
