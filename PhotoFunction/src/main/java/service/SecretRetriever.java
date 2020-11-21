package service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;
import service.googlephoto.GooglePhotoSecret;

import javax.inject.Inject;

public class SecretRetriever {

    private static final String SECRET_NAME = "GooglePhotoSecret";
    private static final String ENDPOINT_TEMPLATE = "secretsmanager.%s.amazonaws.com";
    private static final String REGION = "REGION";

    private final Gson gson;

    @Inject
    public SecretRetriever(Gson gson) {
        this.gson = gson;
    }

    private AWSSecretsManager getClient() {
        final String region = System.getenv(REGION);
        final String endpoint = String.format(ENDPOINT_TEMPLATE, region);

        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setEndpointConfiguration(config);
        return clientBuilder.build();
    }

    public GooglePhotoSecret getGooglePhotoSecret() {

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(SECRET_NAME).withVersionStage("AWSCURRENT");

        GetSecretValueResult getSecretValueResult = getClient().getSecretValue(getSecretValueRequest);

        if(getSecretValueResult == null || getSecretValueResult.getSecretString() == null) {
            new InternalError("SecretValue is absent");
        }

        String secret = getSecretValueResult.getSecretString();
        return gson.fromJson(secret, GooglePhotoSecret.class);
    }
}
