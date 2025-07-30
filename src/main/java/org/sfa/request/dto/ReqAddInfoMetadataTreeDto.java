package org.sfa.request.dto;

import java.util.List;

public class ReqAddInfoMetadataTreeDto {
    private String fieldId;
    private String fieldNameKey;
    private String fieldType;
    private String status;
    private String catId;
    private List<ListItemMetadataDto> listItems;

    // getters and setters
    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldNameKey() {
        return fieldNameKey;
    }

    public void setFieldNameKey(String fieldNameKey) {
        this.fieldNameKey = fieldNameKey;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public List<ListItemMetadataDto> getListItems() {
        return listItems;
    }

    public void setListItems(List<ListItemMetadataDto> listItems) {
        this.listItems = listItems;
    }
}
