package com.xboxkeysale.bot;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class XKSBot extends TelegramLongPollingBot {

    @Autowired
    private Environment environment;

    public XKSBot(@Value("${bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    @Override
    public String getBotUsername() {
        return environment.getProperty("bot.username");
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().toLowerCase();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> sendStartMessage(chatId);
                case "/help" -> sendHelpMessage(chatId);
                case "цены" -> sendGamesMessage(chatId);
                default -> findGameSendMessage(chatId, messageText);
            }
        }
    }

    private void sendStartMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("""
                Привет! Я телеграм-бот канала @XboxKeySale.
                Напишите:
                "/help" - для получения справочной информации
                *название_игры* - поиск игры и цены
                "Цены" - информация обо всех имеющихся играх и их ценах
                """);
        try {
            execute(message);
            log.info("");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendHelpMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("""
                "/help" - для получения справочной информации
                *название_игры* - поиск игры и цены
                Писать на английском (как в оригинале). Можно искать по первым словам - будут предложены возможные варианты
                "Цены" - информация обо всех имеющихся играх и их ценах
                Наш канал в Telegram с новостями - @XboxKeySale
                """);
        try {
            execute(message);
            log.info("");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }


    private void sendPricesMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String textLine;
        Matcher matcher;
        StringBuilder stringBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("\\d+$");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/static/games-and-prices.txt"))) {
            while ((textLine = bufferedReader.readLine()) != null) {
                matcher = pattern.matcher(textLine);
                if (matcher.find()) {
                    String price = matcher.group();
                    stringBuilder.append((Integer.parseInt(price) + 200));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        message.setText(stringBuilder.toString());
        try {
            execute(message);
            log.info("");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

    }

    private void findGameSendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        StringBuilder messageToSend = new StringBuilder();
        String textLine;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/static/games-and-prices.txt"))) {
            while ((textLine = bufferedReader.readLine()) != null) {
                if (textLine.trim().toLowerCase().startsWith(messageText)) {
                    messageToSend.append(textLine);
                    messageToSend.append("\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!messageToSend.toString().isEmpty()) {
            message.setText(messageToSend.toString());
        } else {
            message.setText("Запрашиваемой игры не найдено :(\nУбедитесь в правильности написания или поищите в полном списке");
        }
        try {
            execute(message);
            log.info("Отправлено сообщение с ответом о поиске игры");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Отправляет сообщений со списком всех игр
     *
     * @param chatId в какой чат отправить сообщение
     */
    private void sendGamesMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        StringBuilder stringBuilder = new StringBuilder();
        String textLine;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/static/games-and-prices.txt"))) {
            int stringsCounter = 0;
            while ((textLine = bufferedReader.readLine()) != null) {
                stringsCounter++;
                if (stringsCounter % 50 == 0) {
                    try {
                        message.setText(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                        execute(message);
                        log.info("Отправлено сообщение с частью списка всех игр");
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
                stringBuilder.append(textLine);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        message.setText(stringBuilder.toString());
        try {
            execute(message);
            log.info("Отправлено сообщение со списком всех игр");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}