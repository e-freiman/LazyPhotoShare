package service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.gson.Gson;
import service.googlephoto.GooglePhotoSecret;
import service.telegram.TelegramSecret;

import javax.inject.Inject;

public class SecretRetriever {

    private static final String GOOGLE_PHOTO_SECRET_NAME = "GooglePhotoSecret";
    private static final String TELEGRAM_SECRET_NAME = "TelegramSecret";
    private static final String ENDPOINT_TEMPLATE = "secretsmanager.%s.amazonaws.com";

    private final Gson gson;

    private GooglePhotoSecret googlePhotoSecretCache = null;
    private TelegramSecret telegramSecretCache = null;

    @Inject
    public SecretRetriever(Gson gson) {
        this.gson = gson;
    }

    private <T> T secret(final String secretName, final Class<T> secretClass) {
        final String region = System.getenv("AWS_REGION");
        final String endpoint = String.format(ENDPOINT_TEMPLATE, region);

        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setEndpointConfiguration(config);
        AWSSecretsManager client = clientBuilder.build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName).withVersionStage("AWSCURRENT");

        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        if(getSecretValueResult == null || getSecretValueResult.getSecretString() == null) {
            new InternalError("SecretValue is absent");
        }

        String secret = getSecretValueResult.getSecretString();
        return gson.fromJson(secret, secretClass);
    }

    public GooglePhotoSecret googlePhotoSecret() {
        if (googlePhotoSecretCache == null) {
            googlePhotoSecretCache = secret(GOOGLE_PHOTO_SECRET_NAME, GooglePhotoSecret.class);
        }
        return googlePhotoSecretCache;
    }

    public TelegramSecret telegramSecret() {
        if (telegramSecretCache == null) {
            telegramSecretCache = secret(TELEGRAM_SECRET_NAME, TelegramSecret.class);
        }
        return telegramSecretCache;
    }
}
