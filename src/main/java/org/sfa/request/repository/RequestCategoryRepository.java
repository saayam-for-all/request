package org.sfa.request.repository;

import org.sfa.request.model.entity.RequestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestCategoryRepository extends JpaRepository<RequestCategory, Integer> {
}
