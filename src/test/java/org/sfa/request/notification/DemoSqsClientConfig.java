package org.sfa.request.notification;

import org.sfa.request.config.AwsClientSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * The application only needs the async SQS client to publish. The demo test also
 * inspects the queue, which is simpler with the synchronous client.
 */
@TestConfiguration
public class DemoSqsClientConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public SqsClient sqsClient() {
        return AwsClientSupport
                .applyOverrides(SqsClient.builder().region(Region.of(region)), endpointOverride)
                .build();
    }
}
