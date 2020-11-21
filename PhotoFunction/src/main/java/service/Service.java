package service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.photos.types.proto.Album;
import service.googlephoto.GooglePhotoProxy;
import service.googlephoto.GooglePhotoSecret;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class Service {

    private final SecretRetriever secretRetriever;
    private final GooglePhotoProxy googlePhotoProxy;

    private final Gson gson;

    @Inject
    public Service(SecretRetriever secretRetriever, GooglePhotoProxy googlePhotoProxy, Gson gson) {
        this.gson = gson;
        this.secretRetriever = secretRetriever;
        this.googlePhotoProxy = googlePhotoProxy;
    }

    public void serve(final Context context, final LambdaLogger logger) throws IOException {
        GooglePhotoSecret googlePhotoSecret = secretRetriever.getGooglePhotoSecret();
        List<Album> albums = googlePhotoProxy.getAlbums(googlePhotoSecret);
        for (Album album : albums) {
            logger.log(album.getTitle());
        }
    }
}
