package org.sfa.request.service.impl;

import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.HelpCategoryMapDto;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.repository.HelpCategoryMapRepository;
import org.sfa.request.repository.HelpCategoryRepository;
import org.sfa.request.repository.ReqAddInfoMetadataRepository;
import org.sfa.request.service.api.HelpCategoryService;
import org.sfa.request.utils.NaturalOrderComparator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HelpCategoryServiceImpl implements HelpCategoryService {

    private final HelpCategoryRepository helpCategoryRepository;
    private final HelpCategoryMapRepository helpCategoryMapRepository;
    private final ReqAddInfoMetadataRepository metadataRepo;

    public HelpCategoryServiceImpl(HelpCategoryRepository helpCategoryRepository,
                                   HelpCategoryMapRepository helpCategoryMapRepository,ReqAddInfoMetadataRepository metadataRepo) {
        this.helpCategoryRepository = helpCategoryRepository;
        this.helpCategoryMapRepository = helpCategoryMapRepository;
        this.metadataRepo = metadataRepo;
    }

    @Override
    public List<HelpCategoryMapDto> getChildMappingsByParentId(String parentId) {
        return helpCategoryMapRepository.findByParentId(parentId).stream().map(map -> {
            HelpCategoryMapDto dto = new HelpCategoryMapDto();
            dto.setParentId(map.getParentId());
            dto.setChildId(map.getChildId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getHelpCategoriesMap() {
        return null;
    }

    @Override
    public Map<String, List<String>> getHelpCategoriesTree() {
        List<HelpCategory> allCategories = helpCategoryRepository.findAll();

        Map<String, List<String>> tree = new TreeMap<>(new NaturalOrderComparator());

        for (HelpCategory cat : allCategories) {
            String catId = cat.getCatId().trim().replace("\uFEFF", "");
            int lastDot = catId.lastIndexOf('.');

            String parentId = lastDot != -1 ? catId.substring(0, lastDot) : null;

            if (parentId != null) {
                tree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(catId);
            }
        }

        return tree;
    }



    @Override
    public List<HelpCategory> getCategoriesByCatId(String catId) {
        return helpCategoryRepository.findByCatIdStartingWith(catId);
    }

    @Override
    public List<HelpCategoryDto> getAllHierarchicalCategories() {

        List<HelpCategory> allCategories = helpCategoryRepository.findAll();
        NaturalOrderComparator comparator = new NaturalOrderComparator();

        Map<String, HelpCategoryDto> dtoMap = new HashMap<>();
        for (HelpCategory cat : allCategories) {
            String cleanId = clean(cat.getCatId());
            HelpCategoryDto dto = new HelpCategoryDto(cat);
            dto.setCatId(cleanId);
            dtoMap.put(cleanId, dto);
        }

        List<HelpCategoryDto> rootCategories = new ArrayList<>();

        for (HelpCategoryDto dto : dtoMap.values()) {
            String id = dto.getCatId();
            int lastDot = id.lastIndexOf(".");

            if (lastDot == -1) {
                rootCategories.add(dto);
            } else {
                String parentId = id.substring(0, lastDot);
                HelpCategoryDto parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getSubCategories().add(dto);
                } else {
                    rootCategories.add(dto);
                }
            }
        }

        sortRecursively(rootCategories, comparator);

        return rootCategories;
    }

    private String clean(String id) {
        if (id == null) return null;
        return id
                .trim()
                .replace("\uFEFF", "")
                .replace("\u00A0", ""); // NBSP
    }

    private void sortRecursively(List<HelpCategoryDto> list, NaturalOrderComparator comparator) {

        list.sort(Comparator.comparing(
                HelpCategoryDto::getCatId,
                comparator
        ));

        for (HelpCategoryDto dto : list) {
            if (dto.getSubCategories() != null && !dto.getSubCategories().isEmpty()) {
                sortRecursively(dto.getSubCategories(), comparator);
            }
        }
    }


}
