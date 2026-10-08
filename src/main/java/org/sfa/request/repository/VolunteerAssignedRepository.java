package org.sfa.request.repository;

import org.sfa.request.model.entity.VolunteerAssigned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerAssignedRepository
        extends JpaRepository<VolunteerAssigned, Integer> {

    List<VolunteerAssigned> findByRequestId(String requestId);

    boolean existsByRequestIdAndVolunteerId(
            String requestId,
            String volunteerId
    );
}