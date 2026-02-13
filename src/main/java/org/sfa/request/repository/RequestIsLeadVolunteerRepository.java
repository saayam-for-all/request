package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestIsLeadVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestIsLeadVolunteerRepository extends JpaRepository<RequestIsLeadVolunteer, Integer> {
}
