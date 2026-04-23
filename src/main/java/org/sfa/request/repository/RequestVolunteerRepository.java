package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestVolunteerRepository extends JpaRepository<RequestVolunteer, Long> {
    List<RequestVolunteer> findByRequest(Request request);
    Optional<RequestVolunteer> findByRequestAndVolunteerUserId(Request request, String volunteerUserId);
}
