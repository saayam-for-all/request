package org.sfa.request.service.api;

import org.sfa.request.dto.ReqAddInfoMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.model.entity.ReqAddInfoMetadata;

import java.util.List;
import java.util.Map;

public interface MetadataService {
    List<ReqAddInfoMetadataDto> getMetadataByCategoryId(String catId);
    Map<String, List<ReqAddInfoMetadataTreeDto>> getFullMetadataTree();

}
