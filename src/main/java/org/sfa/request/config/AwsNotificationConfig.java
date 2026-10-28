package org.sfa.request.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsNotificationConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    /** Optional. Set only when running against a local AWS emulator. */
    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public SesClient sesClient() {
        return AwsClientSupport
                .applyOverrides(SesClient.builder().region(Region.of(region)), endpointOverride)
                .build();
    }

    @Bean
    public SnsClient snsClient() {
        return AwsClientSupport
                .applyOverrides(SnsClient.builder().region(Region.of(region)), endpointOverride)
                .build();
    }
}
