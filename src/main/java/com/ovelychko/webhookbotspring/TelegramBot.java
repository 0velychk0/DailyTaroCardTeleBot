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

import java.io.File;
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
    private final String DEBUG_GET_NUM = "debug";
    private final String IMAGE_FILE = "tarotimages/testCard.jpg";
    private final String IMAGE_FILE_URL = "https://raw.githubusercontent.com/0velychk0/DailyTaroCardTeleBot/main/src/main/resources/tarotimages/boxrus.jpg";
    private final int CARDS_COUNT = 78;
    private final String ENDL = "\n";
    private Random random = new Random();

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

            int cardNum = -1;
            boolean debugValue = false;
            if (update.getMessage().getText().startsWith(DEBUG_GET_NUM)) {
                try {
                    cardNum = Integer.parseInt(update.getMessage().getText().substring(DEBUG_GET_NUM.length()).trim());
                    debugValue = true;
                } catch (NumberFormatException ex) {
                }
            }
            if (cardNum < 1 || cardNum > CARDS_COUNT) {
                cardNum = random.nextInt(CARDS_COUNT) + 1;
            }

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
                String cardOfTheDay = property.getProperty("card_" + cardNum);
                joiner.add(cardOfTheDay);
                if (debugValue)
                    joiner.add("debug value: " + cardNum);
            } catch (Exception ex) {
                joiner.add("Exception: " + ex.getMessage());
                logger.info("Exception: {}", ex.getMessage());
            }

            logger.info(joiner.toString());

            try {
                SendPhoto message = new SendPhoto();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setCaption(joiner.toString());
                message.setPhoto(new InputFile(IMAGE_FILE_URL));
                this.execute(message);
            } catch (Exception ex) {
                sendMessage.setText(joiner.toString());
                logger.info(ex.getMessage());
            }
            return sendMessage;
        }
        return null;
    }
}
