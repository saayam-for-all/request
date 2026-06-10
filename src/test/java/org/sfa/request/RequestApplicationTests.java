package org.sfa.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RequestApplicationTests {

    @Test
    void applicationClassCanBeInstantiated() {
        assertDoesNotThrow(RequestApplication::new);
    }
}
