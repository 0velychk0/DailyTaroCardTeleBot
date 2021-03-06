package com.ovelychko.webhookbotspring;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.Futures;
import com.viber.bot.Request;
import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.ViberBot;
import com.viber.bot.message.Message;
import com.viber.bot.message.PictureMessage;
import com.viber.bot.message.TextMessage;
import com.viber.bot.profile.BotProfile;
import com.viber.bot.profile.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class WebhookViberController implements ApplicationListener<ApplicationReadyEvent>, BotController {
    @Inject
    private ViberBot bot;

    @Inject
    private ViberSignatureValidator signatureValidator;

    @Value("${viber.webhook-url}")
    private String webhookUrl;

    @Value("${data-service-viber-url}")
    private String dataServiceUrl;

    @Value("${telegrambot.imageSecurePictorialLink}")
    private String imageSecurePictorialLink;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent appReadyEvent) {

        bot.onConversationStarted(event -> Futures.immediateFuture(Optional.of(
                new TextMessage("Hi " + event.getUser().getName()))));

        bot.onMessageReceived(((event, message, response) -> {
            final UserProfile userProfile = event.getSender();
            StringJoiner joiner = new StringJoiner(ENDL);
            log.info("onUpdateReceived from user '{}' the text message: '{}'",
                    userProfile.getName(), message.toString());

            if (!(message instanceof TextMessage)) {
                log.info("text message supported only");
                return;
            }

            TextMessage textMessage = (TextMessage) message;
            joiner.add("Request: " + textMessage.getText());

            int cardNum = -1;
            boolean debugValue = false;
            if (textMessage.getText().startsWith(DEBUG_GET_NUM)) {
                try {
                    cardNum = Integer.parseInt(textMessage.getText().substring(DEBUG_GET_NUM.length()).trim());
                    debugValue = true;
                } catch (Exception ex) {
                    log.info("Exception: {}", ex.getMessage());
                }
            }
            if (cardNum < 0 || cardNum >= CARDS_COUNT) {
                cardNum = random.nextInt(CARDS_COUNT);
            }

            log.info("user '{}' cardNum: '{}'", userProfile.getName(), cardNum);

            new Thread(new Runnable() {
                public void run() {
                    ViberUserData viberUserData = new ViberUserData(
                            userProfile.getId(),
                            userProfile.getCountry(),
                            userProfile.getLanguage(),
                            userProfile.getApiVersion(),
                            userProfile.getName(),
                            userProfile.getAvatar()
                    );
                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<ViberUserData> request = new HttpEntity<>(viberUserData);
                    ResponseEntity<String> result = restTemplate.postForEntity(dataServiceUrl, request, String.class);
                    log.info(result.toString());
                }
            }).start();

            joiner.add(TarotController.getCardDescription(cardNum, debugValue));

            try {
                Message imageMessage = new PictureMessage(TarotController.getCardImageUrl(imageSecurePictorialLink, cardNum), joiner.toString(), null);
                response.send(imageMessage);
            } catch (Exception ex) {
                response.send(joiner.toString());
                log.info(ex.getMessage());
            }
        }));
    }

    @PostMapping(value = "/viberbot", produces = "application/json")
    public String incoming(@RequestBody String json,
                           @RequestHeader("X-Viber-Content-Signature") String serverSideSignature)
            throws ExecutionException, InterruptedException, IOException {
        Preconditions.checkState(signatureValidator.isSignatureValid(serverSideSignature, json), "invalid signature");
        @Nullable InputStream response = bot.incoming(Request.fromJsonString(json)).get();
        return response != null ? CharStreams.toString(new InputStreamReader(response, Charsets.UTF_16)) : null;
    }

    @Configuration
    @PropertySource("classpath:application.properties")
    public class BotConfiguration {

        @Value("${viber.auth-token}")
        private String authToken;

        @Value("${viber.name}")
        private String name;

        @org.springframework.lang.Nullable
        @Value("${viber.avatar:@null}")
        private String avatar;

        @Bean
        ViberBot viberBot() {
            return new ViberBot(new BotProfile(name, avatar), authToken);
        }

        @Bean
        ViberSignatureValidator signatureValidator() {
            return new ViberSignatureValidator(authToken);
        }
    }
}
