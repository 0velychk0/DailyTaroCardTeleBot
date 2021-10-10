package com.ovelychko.webhookbotspring;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:application.properties")
public class TelegramBotConfig {
    @Value("${telegrambot.webHookPath}")
    String webHookPath;
    @Value("${telegrambot.userName}")
    String userName;
    @Value("${telegrambot.botToken}")
    String botToken;
    @Value("${telegrambot.imageSecurePictorialLink}")
    String imageSecurePictorialLink;
}
