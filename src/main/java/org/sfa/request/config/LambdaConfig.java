package org.sfa.request.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
public class LambdaConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    public SfnClient sfnClient() {
        return SfnClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}