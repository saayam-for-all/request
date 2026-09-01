package org.sfa.request.repository;

import org.sfa.request.model.entity.HelpCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpCategoryRepository extends JpaRepository<HelpCategory, String> {
    List<HelpCategory> findByCatIdStartingWith(String catId);
}
