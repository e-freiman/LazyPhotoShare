package service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import service.database.Chat;
import service.database.DBProxy;
import service.googlephoto.GooglePhotoProxy;
import service.googlephoto.GooglePhotoSecret;
import service.telegram.TelegramProxy;
import service.telegram.TelegramSecret;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {

    private static final Long CHAT_ID = 123L;
    private static final String ALBUM_TITLE = "*TestTitle";
    private static final String MIME_TYPE = "image/jpg";
    private static final String IMAGE_BASE_URL = "testUrl";

    @Mock
    private GooglePhotoProxy googlePhotoProxy;
    @Mock
    private TelegramProxy telegramProxy;
    @Mock
    private DBProxy dbProxy;

    private Service service;

    @Before
    public void setUp() throws IOException {
        service = new Service(googlePhotoProxy, telegramProxy, dbProxy);
    }

    @Test
    public void happyCase() throws IOException {
        Album album = Album.newBuilder()
                .setMediaItemsCount(3L)
                .setTitle(ALBUM_TITLE).build();
        MediaItem mediaItem = MediaItem.newBuilder()
                .setMimeType(MIME_TYPE)
                .setBaseUrl(IMAGE_BASE_URL).build();

        when(telegramProxy.getChats(any())).thenReturn(Collections.singleton(CHAT_ID));
        when(googlePhotoProxy.getAlbums(any())).thenReturn(Collections.singletonList(album));
        when(googlePhotoProxy.getMediaItems(album)).thenReturn(Collections.singletonList(mediaItem));
        when(dbProxy.scan()).thenReturn(Collections.singletonList(new Chat(CHAT_ID)));

        service.serve(mock(Context.class), mock(LambdaLogger.class));

        verify(dbProxy).put(new Chat(CHAT_ID));
        verify(telegramProxy).sendPhoto(any(), eq(CHAT_ID), eq(ALBUM_TITLE.substring(1)), eq(IMAGE_BASE_URL));
    }
}
