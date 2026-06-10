package org.sfa.request.service.api;

import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.HelpCategoryMapDto;
import org.sfa.request.model.entity.HelpCategory;

import java.util.List;
import java.util.Map;

public interface HelpCategoryService {
    Map<String, List<String>> getHelpCategoriesTree();

    List<HelpCategory> getCategoriesByCatId(String catId);
    List<HelpCategoryDto> getAllHierarchicalCategories();

    List<HelpCategoryMapDto> getChildMappingsByParentId(String parentId);

    Map<String, List<String>> getHelpCategoriesMap();



}
