package com.ovelychko.webhookbotspring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.WebhookBot;

@RestController
public class WebhookTelegramController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookTelegramController.class);
    private final WebhookBot telegramBot;

    @Autowired
    public WebhookTelegramController(TelegramBotConfig telegramBotConfig) {
        logger.info("WebhookTelegramController created, telegramBotConfig = {}", telegramBotConfig);
        this.telegramBot = new TelegramBot(telegramBotConfig);
    }

    @PostMapping("/telebot")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        logger.info("WebhookTelegramController.onUpdateReceived called");
        return telegramBot.onWebhookUpdateReceived(update);
    }

    @GetMapping
    public ResponseEntity get() {
        return ResponseEntity.ok().build();
    }
}