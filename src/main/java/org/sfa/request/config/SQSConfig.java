package org.sfa.request.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;

/**
* ClassName: SQSConfig
* Package: org.sfa.request.config
* Description:
*
* @author Fan Peng
* Create 2024/8/14 23:23
* @version 1.0
*/
@Slf4j
@Configuration
public class SQSConfig {

   @Value("${cloud.aws.credentials.access-key}")
   private String accessKey;

   @Value("${cloud.aws.credentials.secret-key}")
   private String secretKey;

   @Value("${cloud.aws.region.static}")
   private String region;

   @Value("${cloud.aws.sqs.endpoint:#{null}}")
   private String sqsEndpoint;

   @Bean
   public SqsAsyncClient sqsAsyncClient() {
       try {
           log.info("Initializing SqsAsyncClient with region: {}", region);
           log.info("SQS Endpoint: {}", sqsEndpoint != null ? sqsEndpoint : "AWS default");
           
           AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
           var builder = SqsAsyncClient.builder()
                   .region(Region.of(region))
                   .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
           
           // If endpoint is configured (for LocalStack), override the default
           if (sqsEndpoint != null && !sqsEndpoint.isEmpty()) {
               builder.endpointOverride(URI.create(sqsEndpoint));
           }
           
           return builder.build();
       } catch (Exception e) {
           log.error("Failed to initialize SqsAsyncClient", e);
           throw e;
       }
   }

   @Bean
   public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
       log.info("Creating SqsTemplate bean");
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