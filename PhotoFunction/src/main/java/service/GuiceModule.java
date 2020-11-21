package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import service.googlephoto.GooglePhotoProxy;

public class GuiceModule extends AbstractModule {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void configure() { }

    @Provides
    public SecretRetriever provideSecretRetriever() {
        return new SecretRetriever(gson);
    }

    public GooglePhotoProxy googlePhotoProxy() {
        return new GooglePhotoProxy();
    }

    @Provides
    public Gson provideGson() {
        return gson;
    }
}
