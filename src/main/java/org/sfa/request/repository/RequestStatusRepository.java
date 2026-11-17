package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Integer> {
    List<RequestStatus> findByStatusIgnoreCase(String status);
}

