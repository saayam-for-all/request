package org.sfa.request.controller;

import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.HelpCategoryMapDto;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.service.api.HelpCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/helpCategories","/dev/requests/v0.0.1/helpCategories"})
public class HelpCategoryController {
    private final HelpCategoryService service;

    public HelpCategoryController(HelpCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HelpCategoryDto>> getAllHierarchical() {
        List<HelpCategoryDto> result = service.getAllHierarchicalCategories();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<HelpCategoryMapDto>> getByParentId(@PathVariable String parentId) {
        List<HelpCategoryMapDto> children = service.getChildMappingsByParentId(parentId);
        return children.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(children);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<List<HelpCategory>> getByCatId(@PathVariable String catId) {
        List<HelpCategory> categories = service.getCategoriesByCatId(catId);
        return categories.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(categories);
    }

    @GetMapping("/categoryMap")
    public ResponseEntity<Map<String, List<String>>> getHelpCategoriesTree() {
        return ResponseEntity.ok(service.getHelpCategoriesTree());
    }

}
