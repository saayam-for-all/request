package org.sfa.request.repository.projection;

public interface MetadataCatFieldItemRow {

    String getCatId();

    String getFieldId();
    String getFieldNameKey();
    String getFieldType();
    String getStatus();

    String getItemId();
    String getItemValue();
    String getItemType();
}
