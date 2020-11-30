package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import service.database.DBProxy;
import service.googlephoto.GooglePhotoProxy;
import service.telegram.TelegramProxy;

public class GuiceModule extends AbstractModule {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final SecretRetriever secretRetriever = new SecretRetriever(gson);

    @Override
    protected void configure() { }

    @Provides
    public GooglePhotoProxy googlePhotoProxy() {
        return new GooglePhotoProxy(secretRetriever.googlePhotoSecret());
    }

    @Provides
    public TelegramProxy telegramProxy() {
        return new TelegramProxy(secretRetriever.telegramSecret(), gson);
    }

    @Provides
    public DBProxy chatRecordsTableProxy() {
        return new DBProxy();
    }


    @Provides
    public Gson provideGson() {
        return gson;
    }
}
