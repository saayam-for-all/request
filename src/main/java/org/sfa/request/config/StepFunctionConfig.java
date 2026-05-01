package org.sfa.request.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.net.URI;

/**
 * Configuration class for AWS Step Functions
 * 
 * @author Saayam Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class StepFunctionConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.stepfunctions.endpoint:#{null}}")
    private String stepFunctionsEndpoint;

    @Bean
    public SfnClient sfnClient() {
        try {
            log.info("Initializing SfnClient with region: {}", region);
            log.info("Step Functions Endpoint: {}", stepFunctionsEndpoint != null ? stepFunctionsEndpoint : "AWS default");

            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            var builder = SfnClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

            // If endpoint is configured (for LocalStack), override the default
            if (stepFunctionsEndpoint != null && !stepFunctionsEndpoint.isEmpty()) {
                builder.endpointOverride(URI.create(stepFunctionsEndpoint));
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to initialize SfnClient", e);
            throw e;
        }
    }
}
