package org.sfa.request.config;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

public class SsmParameterStoreClient implements ParameterStoreClient {

    private final SsmClient ssmClient;

    public SsmParameterStoreClient() {
        this(SsmClient.create());
    }

    SsmParameterStoreClient(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    @Override
    public String getSecureParameter(String parameterPath) {
        return ssmClient.getParameter(GetParameterRequest.builder()
                .name(parameterPath)
                .withDecryption(true)
                .build()).parameter().value();
    }
}
