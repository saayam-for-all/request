package org.sfa.request.config;

import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;

import java.net.URI;

/**
 * Shared AWS client configuration.
 *
 * <p>In AWS, clients use the default credentials chain (IAM role) and the real regional
 * endpoint. When {@code aws.endpoint-override} is set — a local AWS emulator used for
 * development — the endpoint is redirected and static dummy credentials are used, because
 * such emulators require credentials to be present but do not validate them.
 */
public final class AwsClientSupport {

    private AwsClientSupport() {
    }

    public static <B extends AwsClientBuilder<B, ?>> B applyOverrides(B builder, String endpointOverride) {
        if (StringUtils.hasText(endpointOverride)) {
            builder.endpointOverride(URI.create(endpointOverride));
        }
        return builder.credentialsProvider(credentialsProvider(endpointOverride));
    }

    private static AwsCredentialsProvider credentialsProvider(String endpointOverride) {
        if (StringUtils.hasText(endpointOverride)) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
        }
        return DefaultCredentialsProvider.create();
    }
}
