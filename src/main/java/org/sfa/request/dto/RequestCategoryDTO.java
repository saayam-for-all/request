package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.sfa.request.model.entity.HelpCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestCategoryDTO {

    private int requestCategoryId;

    private String catName;
    private String catDesc;

    private List<RequestCategoryDTO> subCategories = new ArrayList<>();

    public RequestCategoryDTO(HelpCategory helpCategory) {
        this.requestCategoryId = Integer.parseInt(helpCategory.getCatId());
        this.catName = helpCategory.getCatName();
        this.catDesc = helpCategory.getCatDesc();
    }
}