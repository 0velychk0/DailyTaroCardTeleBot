package com.ovelychko.webhookbotspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Random;
import java.util.StringJoiner;

@Configuration
public class TelegramBot extends TelegramWebhookBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final TelegramBotConfig telegramBotConfig;
    private final String DEBUG_GET_NUM = "debug";
    private final int CARDS_COUNT = 78;
    private final String ENDL = "\n";
    private final Random random = new Random();

    @Autowired
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
                } catch (Exception ex) {
                    logger.info("Exception: {}", ex.getMessage());
                }
            }
            if (cardNum < 0 || cardNum >= CARDS_COUNT) {
                cardNum = random.nextInt(CARDS_COUNT);
            }

            logger.info("user {} cardNum: {}", update.getMessage().getFrom(), cardNum);

            joiner.add(TarotController.getCardDescription(cardNum, debugValue));

            try {
                SendPhoto message = new SendPhoto();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setCaption(joiner.toString());
                message.setPhoto(new InputFile(TarotController.getCardImageUrl(telegramBotConfig.getImageSecurePictorialLink(), cardNum)));
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
