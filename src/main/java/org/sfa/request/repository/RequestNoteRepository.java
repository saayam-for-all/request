package org.sfa.request.repository;

import org.sfa.request.model.entity.Request;
import org.sfa.request.model.entity.RequestNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestNoteRepository extends JpaRepository<RequestNote, Long> {
    List<RequestNote> findByRequestOrderByCreatedAtDesc(Request request);
}
