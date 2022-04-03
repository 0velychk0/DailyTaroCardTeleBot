package com.ovelychko.webhookbotspring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.StringJoiner;

@RestController
@Slf4j
public class WebhookTelegramController extends TelegramWebhookBot implements BotController {

    @Value("${telegrambot.webHookPath}")
    private String webHookPath;
    @Value("${telegrambot.userName}")
    private String userName;
    @Value("${telegrambot.botToken}")
    private String botToken;
    @Value("${telegrambot.imageSecurePictorialLink}")
    private String imageSecurePictorialLink;
    @Value("${data-service-telegram-url}")
    private String dataServiceUrl;

    @Autowired
    public WebhookTelegramController() {
        log.info("WebhookTelegramController created");
    }

    @PostMapping("/telebot")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.info("WebhookTelegramController.onUpdateReceived called");
        return this.onWebhookUpdateReceived(update);
    }

    public String getBotUsername() {
        return userName;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotPath() {
        return webHookPath;
    }

    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.info("onUpdateReceived");

        StringJoiner joiner = new StringJoiner(ENDL);
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("onUpdateReceived from user '{}' the text message: '{}'",
                    update.getMessage().getFrom().getFirstName(),
                    update.getMessage().getText());

            if (update.getMessage().getFrom().getIsBot()) {
                log.info("Bot user is not supported");
                return null;
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());

            int cardNum = -1;
            boolean debugValue = false;
            if (update.getMessage().getText().startsWith(DEBUG_GET_NUM)) {

                joiner.add("Request: " + update.getMessage().getText());

                try {
                    cardNum = Integer.parseInt(update.getMessage().getText().substring(DEBUG_GET_NUM.length()).trim());
                    debugValue = true;
                } catch (Exception ex) {
                    log.info("Exception: {}", ex.getMessage());
                }
            }
            if (cardNum < 0 || cardNum >= CARDS_COUNT) {
                cardNum = random.nextInt(CARDS_COUNT);
            }

            log.debug("user '{}' cardNum: {}", update.getMessage().getFrom().getFirstName(), cardNum);

            final User user = update.getMessage().getFrom();

            new Thread(new Runnable() {
                public void run() {
                    TelegramUserData telegramUserData = new TelegramUserData(
                            user.getId(),
                            user.getFirstName(),
                            user.getIsBot(),
                            user.getLastName(),
                            user.getUserName(),
                            user.getLanguageCode(),
                            user.getCanJoinGroups(),
                            user.getCanReadAllGroupMessages(),
                            user.getSupportInlineQueries()
                    );

                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<TelegramUserData> request = new HttpEntity<>(telegramUserData);
                    ResponseEntity<String> result = restTemplate.postForEntity(dataServiceUrl, request, String.class);
                    log.info(result.toString());
                }
            }).start();

            joiner.add(TarotController.getCardDescription(cardNum, debugValue));

            try {
                SendPhoto message = new SendPhoto();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setCaption(joiner.toString());
                message.setPhoto(new InputFile(TarotController.getCardImageUrl(imageSecurePictorialLink, cardNum)));
                this.execute(message);
            } catch (Exception ex) {
                sendMessage.setText(joiner.toString());
                log.info(ex.getMessage());
            }
            return sendMessage;
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();
    }
}
