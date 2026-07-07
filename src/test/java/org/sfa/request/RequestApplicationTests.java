package org.sfa.request;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Requires DB connection")
@SpringBootTest
class RequestApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void main() {
        RequestApplication.main(new String[] {});
        assertThat(true).isTrue();
    }
}

