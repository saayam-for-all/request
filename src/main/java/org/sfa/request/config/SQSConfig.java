//package org.sfa.request.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//import io.awspring.cloud.sqs.operations.SqsTemplate;
//import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
//
///**
// * ClassName: SQSConfig
// * Package: org.sfa.request.config
// * Description:
// *
// * @author Fan Peng
// * Create 2024/8/14 23:23
// * @version 1.0
// */
//@Configuration
//public class SQSConfig {
//
//    @Value("${cloud.aws.credentials.access-key}")
//    private String accessKey;
//
//    @Value("${cloud.aws.credentials.secret-key}")
//    private String secretKey;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;
//
//    @Bean
//    public SqsAsyncClient sqsAsyncClient() {
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
//        return SqsAsyncClient.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                .build();
//    }
//
//    @Bean
//    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
//        return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
//    }
//
//    @Bean
//    public SqsMessageListenerContainerFactory<?> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
//        return SqsMessageListenerContainerFactory
//                .builder()
//                .sqsAsyncClient(sqsAsyncClient)
//                .build();
//    }
//}