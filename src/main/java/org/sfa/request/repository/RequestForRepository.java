package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestFor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestForRepository extends JpaRepository<RequestFor, Integer> {
}
