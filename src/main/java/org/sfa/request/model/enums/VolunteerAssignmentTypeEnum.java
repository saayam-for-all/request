package org.sfa.request.model.enums;

import lombok.Getter;

@Getter
public enum VolunteerAssignmentTypeEnum {
    LEAD("LEAD"),
    HELPING("HELPING");

    private final String value;

    VolunteerAssignmentTypeEnum(String value) {
        this.value = value;
    }
}
