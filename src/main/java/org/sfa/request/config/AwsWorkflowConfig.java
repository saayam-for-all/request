package org.sfa.request.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

/**
 * AWS Step Functions client, used to resolve paused workflow states
 * ({@code .waitForTaskToken}) in the help-request workflow (issue #95).
 */
@Configuration
public class AwsWorkflowConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    /** Optional. Set only when running against a local AWS emulator. */
    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public SfnClient sfnClient() {
        return AwsClientSupport
                .applyOverrides(SfnClient.builder().region(Region.of(region)), endpointOverride)
                .build();
    }
}
