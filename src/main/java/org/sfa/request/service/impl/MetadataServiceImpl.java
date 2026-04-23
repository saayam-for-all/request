package org.sfa.request.service.impl;

import org.sfa.request.dto.ListItemMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.sfa.request.repository.ReqAddInfoMetadataRepository;
import org.sfa.request.service.api.MetadataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private final ReqAddInfoMetadataRepository metadataRepo;

    public MetadataServiceImpl(ReqAddInfoMetadataRepository metadataRepo) {
        this.metadataRepo = metadataRepo;
    }

    @Override
    public List<ReqAddInfoMetadataDto> getMetadataByCategoryId(String catId) {
        List<ReqAddInfoMetadata> metadataList = metadataRepo.findByCatId(catId);
        return metadataList.stream().map(meta -> {
            ReqAddInfoMetadataDto dto = new ReqAddInfoMetadataDto();
            dto.setFieldId(meta.getFieldId());
            dto.setFieldNameKey(meta.getFieldNameKey());
            dto.setFieldType(meta.getFieldType());
            dto.setStatus(meta.getStatus());
            dto.setCatId(meta.getCatId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<ReqAddInfoMetadataTreeDto>> getFullMetadataTree() {
        List<ReqAddInfoMetadata> metadataList = metadataRepo.findAll();

        // Use TreeMap to ensure sorted keys
        Map<String, List<ReqAddInfoMetadataTreeDto>> groupedMap = new TreeMap<>();

        for (ReqAddInfoMetadata meta : metadataList) {
            String catId = meta.getCatId();
            ReqAddInfoMetadataTreeDto dto = new ReqAddInfoMetadataTreeDto();

            dto.setFieldId(meta.getFieldId());
            dto.setFieldNameKey(meta.getFieldNameKey());
            dto.setFieldType(meta.getFieldType());
            dto.setStatus(meta.getStatus());
            dto.setCatId(meta.getCatId());

            if (meta.getListItems() != null && !meta.getListItems().isEmpty()) {
                List<ListItemMetadataDto> listItems = meta.getListItems().stream().map(item -> {
                    ListItemMetadataDto itemDto = new ListItemMetadataDto();
                    itemDto.setItemId(item.getItemId());
                    itemDto.setItemValue(item.getItemValue());
                    itemDto.setItemType(item.getItemType());
                    return itemDto;
                }).collect(Collectors.toList());
                dto.setListItems(listItems);
            } else {
                dto.setListItems(null);
            }

            groupedMap.computeIfAbsent(catId, k -> new ArrayList<>()).add(dto);
        }

        return groupedMap;
    }
}

