package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestIsLeadVol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for RequestIsLeadVol entity.
 * Provides CRUD operations and query support.
 */
@Repository
public interface RequestIsLeadVolRepository extends JpaRepository<RequestIsLeadVol, Integer> {
    
    // Optional custom finder methods
    RequestIsLeadVol findByRequestIsLead(String requestIsLead);

}
