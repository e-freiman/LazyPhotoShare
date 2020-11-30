package service.telegram;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TelegramProxy {

    private final TelegramSecret secret;
    private TelegramBot botCache = null;
    private Gson gson;

    public TelegramProxy(final TelegramSecret secret, final Gson gson) {
        this.secret = secret;
        this.gson = gson;
    }

    private TelegramBot getBot() {
        if (botCache == null) {
            botCache = new TelegramBot(secret.getToken());
        }
        return botCache;
    }

    public Optional<Long> getChat(final LambdaLogger logger, String eventBody) {
        if (eventBody == null) {
            return Optional.empty();
        }

        Update update = gson.fromJson(eventBody, Update.class);
        return Optional.of(update.message().chat().id());
    }

    public void sendMessage(final LambdaLogger logger, final Long chatId, final String text) {
        SendResponse response = getBot().execute(new SendMessage(chatId, text));
        logger.log(response.toString());
    }

    public void sendPhoto(final LambdaLogger logger, final Long chatId, final String caption, final String photoUrl) throws IOException {
        byte[] image = recoverImageFromUrl(photoUrl);
        SendResponse response = getBot().execute(new SendPhoto(chatId, image).caption(caption));
    }

    private byte[] recoverImageFromUrl(String urlText) throws IOException {
        URL url = new URL(urlText);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte [] buffer = new byte[ 1024 ];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }

        return output.toByteArray();
    }

}
