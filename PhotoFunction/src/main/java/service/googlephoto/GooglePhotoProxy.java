package service.googlephoto;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.ListAlbumsPagedResponse;
import com.google.photos.types.proto.Album;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GooglePhotoProxy {

    public List<Album> getAlbums(final GooglePhotoSecret secret) throws IOException {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(secret.getClientId(), secret.getClientSecret())
                .setJsonFactory(new JacksonFactory())
                .setTransport(new NetHttpTransport()).build();

        credential.setAccessToken(secret.getAccessToken());
        credential.setRefreshToken(secret.getRefreshToken());
        credential.refreshToken();

        // Set up the Photos Library Client that interacts with the API
        PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.create(
                        new AccessToken(credential.getAccessToken(), new Date(credential.getExpirationTimeMilliseconds())))))
                .build();

        try (PhotosLibraryClient photosLibraryClient = PhotosLibraryClient.initialize(settings)) {
            ListAlbumsPagedResponse response = photosLibraryClient.listAlbums();
            Page page = response.getPage();

            List<Album> albums = new ArrayList<>();

            while (page != null) {

                Iterator<Album> iter = page.getValues().iterator();
                while (iter.hasNext()) {
                    albums.add(iter.next());
                }

                page = page.getNextPage();
            }

            return albums;
        }
    }

}
