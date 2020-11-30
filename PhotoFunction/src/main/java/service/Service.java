package service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import service.database.Chat;
import service.database.DBProxy;
import service.googlephoto.GooglePhotoProxy;
import service.telegram.TelegramProxy;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class Service {

    private static final String ALBUM_PREFIX = "*";

    private final GooglePhotoProxy googlePhotoProxy;
    private final TelegramProxy telegramProxy;
    private final DBProxy DBProxy;
    private final Random random = new Random();
    private final Gson gson;

    @Inject
    public Service(final GooglePhotoProxy googlePhotoProxy,
                   final TelegramProxy telegramProxy,
                   final DBProxy DBProxy,
                   final Gson gson) {
        this.googlePhotoProxy = googlePhotoProxy;
        this.telegramProxy = telegramProxy;
        this.DBProxy = DBProxy;
        this.gson = gson;
    }

    public void serve(APIGatewayProxyRequestEvent event, final Context context, final LambdaLogger logger) throws IOException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        logger.log(String.format("Event: %s", gson.toJson(event)));

        // Read recent messages.
        Optional<Long> chatId = telegramProxy.getChat(logger, event.getBody());

        if (chatId.isPresent()) {
            // Update the database.
            DBProxy.put(new Chat(chatId.get()));
            logger.log(String.format("Chat %d added to database", chatId.get()));
        } else {
            logger.log(String.format("ChatId isn't found in the request"));
        }

        // Retrieve a photo.
        List<Album> albums = googlePhotoProxy.getAlbums(ALBUM_PREFIX);

        if (albums.isEmpty()) {
            logger.log(String.format("No albums with prefix: '%s' found", ALBUM_PREFIX));
            return;
        }

        List<Album> weightedAlbums = new ArrayList<>();
        for (Album album : albums) {
            for (int i = 0; i < album.getMediaItemsCount(); i++) {
                weightedAlbums.add(album);
            }
        }
        Album album = weightedAlbums.get(random.nextInt(weightedAlbums.size()));
        List<MediaItem> items = googlePhotoProxy.getMediaItems(album);

        if (items.isEmpty()) {
            logger.log(String.format("No items in album: %s found", album.getTitle()));
            return;
        }

        MediaItem item;
        do {
            item = items.get(new Random().nextInt(items.size()));
        } while (!item.getMimeType().contains("image"));

        final String albumTitle = album.getTitle().substring(ALBUM_PREFIX.length());
        logger.log(String.format("Album: %s, MediaItem: %s", albumTitle, item.getId()));

        // Retrieve a list of all the chats.
        List<Chat> chats = DBProxy.scan();

        if (chatId.isPresent()) {
            // Send photo to the requester.
            telegramProxy.sendPhoto(logger, chatId.get(), albumTitle, item.getBaseUrl());
            logger.log(String.format("Photo sent to %d chat", chatId.get()));
        } else {
            // Send photo to all the chats.
            for (Chat chat : chats) {
                telegramProxy.sendPhoto(logger, chat.getChatId(), albumTitle, item.getBaseUrl());
                logger.log(String.format("Photo sent to %d chat", chat.getChatId()));
            }
        }
    }
}
