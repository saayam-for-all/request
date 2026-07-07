package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestGuestDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestGuestDetailsRepository extends JpaRepository<RequestGuestDetails, String> {
}
