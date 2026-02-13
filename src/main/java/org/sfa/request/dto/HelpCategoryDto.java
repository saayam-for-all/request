package org.sfa.request.dto;

import org.sfa.request.model.entity.HelpCategory;

import java.util.ArrayList;
import java.util.List;

public class HelpCategoryDto {
    private String catId;
    private String catName;
    private String catDesc;
    private List<HelpCategoryDto> subCategories = new ArrayList<>();

    public HelpCategoryDto() {}

    public HelpCategoryDto(HelpCategory category) {
        this.catId = category.getCatId();
        this.catName = category.getCatName();
        this.catDesc = category.getCatDesc();
        this.subCategories = new ArrayList<>();
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatDesc() {
        return catDesc;
    }

    public void setCatDesc(String catDesc) {
        this.catDesc = catDesc;
    }

    public List<HelpCategoryDto> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<HelpCategoryDto> subCategories) {
        this.subCategories = subCategories;
    }
}
