package org.sfa.request.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SQSConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    /** Optional. Set only for local runs against LocalStack. */
    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return AwsClientSupport
                .applyOverrides(SqsAsyncClient.builder().region(Region.of(region)), endpointOverride)
                .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<?> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}
