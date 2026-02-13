package org.sfa.request.service.impl;

import org.sfa.request.dto.ListItemMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.model.entity.ListItemMetadata;
import org.sfa.request.model.entity.ReqAddInfoMetadata;
import org.sfa.request.repository.ListItemMetadataRepository;
import org.sfa.request.repository.ReqAddInfoMetadataRepository;
import org.sfa.request.service.api.MetadataService;
import org.sfa.request.utils.NaturalOrderComparator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private final ReqAddInfoMetadataRepository metadataRepo;
    private final ListItemMetadataRepository listItemRepo;

    public MetadataServiceImpl(ReqAddInfoMetadataRepository metadataRepo,
                               ListItemMetadataRepository listItemRepo) {
        this.metadataRepo = metadataRepo;
        this.listItemRepo = listItemRepo;
    }

    @Override
    public List<ReqAddInfoMetadataDto> getMetadataByCategoryId(String catId) {
        List<ReqAddInfoMetadata> fields = metadataRepo.findByCatId(catId);
        fields.sort(Comparator.comparing(ReqAddInfoMetadata::getFieldId, new NaturalOrderComparator()));
        return fields.stream().map(this::mapToDtoNoItems).collect(Collectors.toList());
    }

    @Override
    public List<ReqAddInfoMetadataTreeDto> getMetadataCategoryTree() {
        List<ReqAddInfoMetadata> all = metadataRepo.findAll();

        all.sort(Comparator
                .comparing(ReqAddInfoMetadata::getCatId, new NaturalOrderComparator())
                .thenComparing(ReqAddInfoMetadata::getFieldId, new NaturalOrderComparator())
        );

        Map<String, List<ReqAddInfoMetadataDto>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        ReqAddInfoMetadata::getCatId,
                        LinkedHashMap::new,
                        Collectors.mapping(this::mapToDtoNoItems, Collectors.toList())
                ));

        return grouped.entrySet()
                .stream()
                .map(entry -> {
                    ReqAddInfoMetadataTreeDto dto = new ReqAddInfoMetadataTreeDto();
                    dto.setCatId(entry.getKey());
                    dto.setFields(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ReqAddInfoMetadataTreeDto getMetadataFormByCategoryId(String catId) {
        List<ReqAddInfoMetadata> fields = metadataRepo.findByCatId(catId);

        fields.sort(Comparator.comparing(ReqAddInfoMetadata::getFieldId, new NaturalOrderComparator()));

        List<String> fieldIds = fields.stream()
                .map(ReqAddInfoMetadata::getFieldId)
                .filter(Objects::nonNull)
                .toList();

        Map<String, List<ListItemMetadataDto>> itemsByFieldId = new HashMap<>();
        if (!fieldIds.isEmpty()) {
            List<ListItemMetadata> items = listItemRepo.findByFieldIdIn(fieldIds);

            items.sort(Comparator.comparing(ListItemMetadata::getItemId, new NaturalOrderComparator()));

            itemsByFieldId = items.stream()
                    .collect(Collectors.groupingBy(
                            ListItemMetadata::getFieldId,
                            HashMap::new,
                            Collectors.mapping(this::mapItemToDto, Collectors.toList())
                    ));
        }

        List<ReqAddInfoMetadataDto> fieldDtos = new ArrayList<>();
        for (ReqAddInfoMetadata meta : fields) {
            ReqAddInfoMetadataDto dto = mapToDtoNoItems(meta);
            dto.setListItems(itemsByFieldId.getOrDefault(meta.getFieldId(), Collections.emptyList()));
            fieldDtos.add(dto);
        }

        ReqAddInfoMetadataTreeDto response = new ReqAddInfoMetadataTreeDto();
        response.setCatId(catId);
        response.setFields(fieldDtos);
        return response;
    }

    private ReqAddInfoMetadataDto mapToDtoNoItems(ReqAddInfoMetadata meta) {
        ReqAddInfoMetadataDto dto = new ReqAddInfoMetadataDto();
        dto.setFieldId(meta.getFieldId());
        dto.setFieldNameKey(meta.getFieldNameKey());
        dto.setFieldType(meta.getFieldType());
        dto.setStatus(meta.getStatus());
        dto.setCatId(meta.getCatId());
        dto.setListItems(Collections.emptyList());
        return dto;
    }

    private ListItemMetadataDto mapItemToDto(ListItemMetadata item) {
        ListItemMetadataDto dto = new ListItemMetadataDto();
        dto.setItemId(item.getItemId());
        dto.setItemValue(item.getItemValue());
        dto.setItemType(item.getItemType());
        return dto;
    }
    @Override
    public List<ReqAddInfoMetadataTreeDto> getAllMetadataWithItems() {

        List<org.sfa.request.repository.projection.MetadataCatFieldItemRow> rows =
                metadataRepo.fetchAllMetadataWithItems();

        Map<String, Map<String, ReqAddInfoMetadataDto>> catFieldMap = new LinkedHashMap<>();

        for (var r : rows) {
            catFieldMap.putIfAbsent(r.getCatId(), new LinkedHashMap<>());

            Map<String, ReqAddInfoMetadataDto> fields = catFieldMap.get(r.getCatId());

            ReqAddInfoMetadataDto fieldDto = fields.computeIfAbsent(r.getFieldId(), fid -> {
                ReqAddInfoMetadataDto dto = new ReqAddInfoMetadataDto();
                dto.setFieldId(r.getFieldId());
                dto.setFieldNameKey(r.getFieldNameKey());
                dto.setFieldType(r.getFieldType());
                dto.setStatus(r.getStatus());
                dto.setCatId(r.getCatId());
                dto.setListItems(new ArrayList<>());
                return dto;
            });

            if (r.getItemId() != null) {
                ListItemMetadataDto itemDto = new ListItemMetadataDto();
                itemDto.setItemId(r.getItemId());
                itemDto.setItemValue(r.getItemValue());
                itemDto.setItemType(r.getItemType());
                fieldDto.getListItems().add(itemDto);
            }
        }

        List<ReqAddInfoMetadataTreeDto> result = new ArrayList<>();

        for (var catEntry : catFieldMap.entrySet()) {
            ReqAddInfoMetadataTreeDto catDto = new ReqAddInfoMetadataTreeDto();
            catDto.setCatId(catEntry.getKey());
            catDto.setFields(new ArrayList<>(catEntry.getValue().values()));
            result.add(catDto);
        }

        return result;
    }

}
