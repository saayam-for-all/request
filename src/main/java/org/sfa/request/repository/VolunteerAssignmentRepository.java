package org.sfa.request.repository;

import org.sfa.request.model.entity.VolunteerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerAssignmentRepository extends JpaRepository<VolunteerAssignment, Long> {

    List<VolunteerAssignment> findByRequestId(String requestId);

    Optional<VolunteerAssignment> findByRequestIdAndVolunteerType(String requestId, String volunteerType);

    Optional<VolunteerAssignment> findByRequestIdAndVolunteerId(String requestId, String volunteerId);

    boolean existsByRequestIdAndVolunteerIdAndVolunteerType(String requestId, String volunteerId, String volunteerType);
}
