package org.sfa.request.repository;

import org.sfa.request.model.SaayamRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaayamRequestRepository extends JpaRepository<SaayamRequest, Long> {
} 