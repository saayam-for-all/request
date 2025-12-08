package org.sfa.request.dto;

import org.sfa.request.model.entity.HelpCategory;
import java.util.ArrayList;
import java.util.List;

public class HelpCategoryDto {

    private String catId;
    private String catName;
    private String catDesc;
    private List<HelpCategoryDto> subCategories = new ArrayList<>();

    // Default constructor
    public HelpCategoryDto() {}

    // Constructor using entity
    public HelpCategoryDto(HelpCategory helpCategory) {
        this.catId = helpCategory.getCatId();
        this.catName = helpCategory.getCatName();
        this.catDesc = helpCategory.getCatDesc();
    }

    // Getters and Setters
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

