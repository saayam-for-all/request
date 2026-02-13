package org.sfa.request.dto;

import java.util.List;

public class ReqAddInfoMetadataTreeDto {
    private String catId;
    private List<ReqAddInfoMetadataDto> fields;

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public List<ReqAddInfoMetadataDto> getFields() {
        return fields;
    }

    public void setFields(List<ReqAddInfoMetadataDto> fields) {
        this.fields = fields;
    }
}
