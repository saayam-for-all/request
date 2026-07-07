package org.sfa.request.service.api;

import org.sfa.request.dto.ReqAddInfoMetadataDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;

import java.util.List;

public interface MetadataService {

    List<ReqAddInfoMetadataDto> getMetadataByCategoryId(String catId);
    List<ReqAddInfoMetadataTreeDto> getMetadataCategoryTree();

    ReqAddInfoMetadataTreeDto getMetadataFormByCategoryId(String catId);

    List<ReqAddInfoMetadataTreeDto> getAllMetadataWithItems();

}
