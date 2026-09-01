package org.sfa.request.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqAddInfoDTO {

    private String fieldId;       // e.g. "5.2.A"
    private String fieldType;     // "list", "string", "int", "float"
    private List<String> selectedItems;  // for list-type: ["5.2.A.1", "5.2.A.2"]
    private String fieldValue;    // for value-type: "4"
}