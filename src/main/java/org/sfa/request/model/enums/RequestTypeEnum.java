package org.sfa.request.model.enums;

import lombok.Getter;

@Getter
public enum RequestTypeEnum {
    UNSPECIFIED(0),
    IN_PERSON(1),
    REMOTE(2);

    private final int id;

    RequestTypeEnum(int id) {
        this.id = id;
    }
}


