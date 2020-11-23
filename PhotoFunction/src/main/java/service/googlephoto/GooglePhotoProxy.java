package service.googlephoto;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.SearchMediaItemsPagedResponse;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.ListAlbumsPagedResponse;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GooglePhotoProxy {

    private final GooglePhotoSecret secret;
    private PhotosLibraryClient clientCache = null;

    private PhotosLibraryClient getClient() throws IOException {
        if (clientCache == null) {
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

            clientCache = PhotosLibraryClient.initialize(settings);
        }
        return clientCache;
    }

    public GooglePhotoProxy(final GooglePhotoSecret secret) {
        this.secret = secret;
    }

    public List<Album> getAlbums(final String prefix) throws IOException {
        ListAlbumsPagedResponse response = getClient().listAlbums();
        List<Album> albums = new ArrayList<>();
        for (Album album : response.iterateAll()) {
            if (album.getTitle().startsWith(prefix)) {
                albums.add(album);
            }
        }
        return albums;
    }

    public List<MediaItem> getMediaItems(final Album album) throws IOException {
        List<MediaItem> mediaItems = new ArrayList<>();
        SearchMediaItemsPagedResponse response = getClient().searchMediaItems(album.getId());
        for (MediaItem item : response.iterateAll()) {
            mediaItems.add(item);
        }
        return mediaItems;
    }

    /*public byte[] getPhoto(final MediaItem mediaItem) throws IOException {
        MediaItem item = getClient(). getMediaItem(mediaItem.getId());
        item
    }*/
}
