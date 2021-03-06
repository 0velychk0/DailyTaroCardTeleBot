package com.ovelychko.webhookbotspring;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import java.util.StringJoiner;

@Slf4j
public class TarotController {
    private static final String CARD_FILE = "tarotcards.properties";
    private static final String DEBUG_GET_NUM = "debug";
    private static final int CARDS_COUNT = 78;
    private static final String ENDL = "\n";
    private static final Random random = new Random();

    public static String getCardImageUrl(String template, int cardNum) {
        String fileName = String.format(template, cardNum);
        return fileName;
    }

    public static String getCardDescription(int cardNum, boolean debugValue) {
        StringJoiner joiner = new StringJoiner(ENDL);
        try {
            ClassLoader classLoader = TarotController.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(CARD_FILE);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            if (inputStream == null) {
                joiner.add("Sorry, unable to find " + CARD_FILE);
                log.info("Sorry, unable to find " + CARD_FILE);
                return joiner.toString();
            }

            Properties property = new Properties();
            property.load(reader);
            joiner.add(property.getProperty("greeting"));

            if (debugValue)
                joiner.add("debug value: " + cardNum);
            else
                joiner.add(property.getProperty("random_card"));

            String cardOfTheDay = property.getProperty("card_" + cardNum);
            joiner.add(cardOfTheDay);
        } catch (Exception ex) {
            joiner.add("Exception: " + ex.getMessage());
            log.info("Exception: {}", ex.getMessage());
        }
        return joiner.toString();
    }
}
