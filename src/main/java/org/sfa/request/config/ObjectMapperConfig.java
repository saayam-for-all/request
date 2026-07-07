package org.sfa.request.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * ClassName: ObjectMapperConfig
 * Package: org.sfa.request.config
 * Description:
 *
 * @author Fan Peng
 * Create 2024/7/8 19:40
 * @version 1.0
 */
@UtilityClass
public class ObjectMapperConfig {
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

}
