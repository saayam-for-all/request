package org.sfa.request.controller;

import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.HelpCategoryMapDto;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.service.api.HelpCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/parent")
    public ResponseEntity<List<HelpCategoryMapDto>> getByParentId(@RequestBody Map<String, String> request) {

        String parentId = request.get("parentId");
        if (parentId == null || parentId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<HelpCategoryMapDto> children = service.getChildMappingsByParentId(parentId);
        return children.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(children);
    }

    @PostMapping("/byId")
    public ResponseEntity<List<HelpCategory>> getByCatId(@RequestBody Map<String, String> request) {

        String catId = request.get("catId");
        if (catId == null || catId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<HelpCategory> categories = service.getCategoriesByCatId(catId);
        return categories.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(categories);
    }

    @GetMapping("/categoryMap")
    public ResponseEntity<Map<String, List<String>>> getHelpCategoriesTree() {
        return ResponseEntity.ok(service.getHelpCategoriesTree());
    }

}
