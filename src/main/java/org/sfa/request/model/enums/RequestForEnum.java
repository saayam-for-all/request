package org.sfa.request.model.enums;

import lombok.Getter;

@Getter
public enum RequestForEnum {
    UNSPECIFIED(0),
    SELF(1),
    OTHER(2);

    private final int id;

    RequestForEnum(int id) {
        this.id = id;
    }
}



