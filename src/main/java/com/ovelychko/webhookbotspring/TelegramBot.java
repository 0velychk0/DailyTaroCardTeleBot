package com.ovelychko.webhookbotspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.StringJoiner;

@Configuration
public class TelegramBot extends TelegramWebhookBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final TelegramBotConfig telegramBotConfig;
    private final String CARD_FILE = "tarotcards.properties";
    private final String IMAGE_FILE = "tarotimages/testCard.jpg";
    private final int CARDS_COUNT = 78;
    private final String ENDL = "\n";

    public TelegramBot(TelegramBotConfig telegramBotConfig) {
        logger.info("TelegramBot created, telegramBotConfig = {}", telegramBotConfig);
        this.telegramBotConfig = telegramBotConfig;
    }

    public String getBotUsername() {
        return telegramBotConfig.getUserName();
    }

    public String getBotToken() {
        return telegramBotConfig.getBotToken();
    }

    public String getBotPath() {
        return telegramBotConfig.getWebHookPath();
    }

    public BotApiMethod onWebhookUpdateReceived(Update update) {
        logger.info("onUpdateReceived");

        StringJoiner joiner = new StringJoiner(ENDL);
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("onUpdateReceived from user {} the text message: {}",
                    update.getMessage().getFrom().getFirstName(),
                    update.getMessage().getText());
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(CARD_FILE);
                Reader reader = new InputStreamReader(inputStream, "UTF-8");

                if (inputStream == null) {
                    joiner.add("Sorry, unable to find " + CARD_FILE);
                    logger.info("Sorry, unable to find " + CARD_FILE);
                    sendMessage.setText(joiner.toString());
                    return sendMessage;
                }

                Properties property = new Properties();
                property.load(reader);
                joiner.add(property.getProperty("greeting"));
                joiner.add(property.getProperty("random_card"));
                Random random = new Random();
                int num = random.nextInt(CARDS_COUNT + 1) + 1;
                String cardOfTheDay = property.getProperty("card_" + num);
                joiner.add(cardOfTheDay);
                joiner.add("debug value: " + num);
            } catch (Exception ex) {
                joiner.add("Exception: " + ex.getMessage());
                logger.info("Exception: {}", ex.getMessage());
            }
            sendMessage.setText(joiner.toString());
            logger.info(joiner.toString());

            try {
                ClassLoader classLoader = getClass().getClassLoader();
                URL url = classLoader.getResource(IMAGE_FILE);

                SendPhoto message = new SendPhoto();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setCaption("Caption Photo");
                message.setPhoto(new InputFile(url.openStream(), "File Name Photo"));
                this.execute(message);
            } catch (Exception ex) {
                logger.info(ex.getMessage());
            }
            return sendMessage;
        }
        return null;
    }
}
