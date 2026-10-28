package org.sfa.request.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sfa.request.stepfunctions.WorkflowExecutionStarter;
import org.sfa.request.stepfunctions.WorkflowSubmissionAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
@ConditionalOnProperty(name = "saayam.step-functions.enabled", havingValue = "true")
public class StepFunctionsConfig {
    @Bean
    public SfnClient sfnClient(@Value("${cloud.aws.region.static}") String region,
                               @Value("${aws.endpoint-override:}") String endpointOverride) {
        return AwsClientSupport.applyOverrides(SfnClient.builder().region(Region.of(region)), endpointOverride).build();
    }

    @Bean
    public WorkflowSubmissionAdapter workflowSubmissionAdapter(ObjectMapper mapper) {
        return new WorkflowSubmissionAdapter(mapper);
    }

    @Bean
    public WorkflowExecutionStarter workflowExecutionStarter(SfnClient client, ObjectMapper mapper,
            @Value("${saayam.step-functions.state-machine-arn:}") String arn) {
        return new WorkflowExecutionStarter(client, mapper, arn);
    }
}
