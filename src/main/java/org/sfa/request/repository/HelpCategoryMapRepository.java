package org.sfa.request.repository;

import org.sfa.request.model.entity.HelpCategoryMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpCategoryMapRepository extends JpaRepository<HelpCategoryMap, String> {
    List<HelpCategoryMap> findByParentId(String parentId);
}