package com.xboxkeysale.bot;

import java.io.*;

import com.xboxkeysale.bot.commands.BotCommands;
import com.xboxkeysale.bot.commands.Buttons;
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

/**
 * Класс для обработки сообщений бота
 *
 * @author Igor Golovkov
 */
@Slf4j
@Component
public class XKSBot extends TelegramLongPollingBot implements BotCommands {

    /**
     * Объект для перменных среды, содержащий токен бота и его название
     */
    @Autowired
    private Environment environment;

    /**
     * Конструктор для инициализации бота
     *
     * @param botToken Токен бота
     */
    public XKSBot(@Value("${bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    /**
     * Геттер для получения названия бота
     *
     * @return Название бота
     */
    @Override
    public String getBotUsername() {
        return environment.getProperty("bot.username");
    }

    /**
     * Метод, срабатывающий при получении сообщения
     *
     * @param update Сообщение-обновление
     */
    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        String receivedMessage;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText().trim().toLowerCase();
                botAnswerUtils(receivedMessage, chatId);
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            receivedMessage = update.getCallbackQuery().getData().trim().toLowerCase();
            botAnswerUtils(receivedMessage, chatId);
        }
    }

    /**
     * Метод для ответов в зависимости от полученного сообщения
     *
     * @param receivedMessage Полученное от пользователя сообщение
     * @param chatId          Код чата с пользователем
     */
    private void botAnswerUtils(String receivedMessage, long chatId) {
        switch (receivedMessage) {
            case "/start" -> sendDefaultMessage(chatId, START_TEXT);
            case "/help" -> sendDefaultMessage(chatId, HELP_TEXT);
            case "цены" -> sendAllGamesMessage(chatId);
            default -> findGameSendMessage(chatId, receivedMessage);
        }
    }

    /**
     * Метод для отправки предопределённого сообщения со справочной информацией
     *
     * @param chatId        Код чата с пользователем
     * @param messageToSend Сообщение для отправки
     */
    private void sendDefaultMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageToSend);
        message.setReplyMarkup(Buttons.inlineMarkup());

        try {
            execute(message);
            log.info("Отправлено сообщение со стандартной информацией " + chatId);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Метод для отправки списка всех игр из txt-файла
     * Список дробится по 50 строк, чтобы не превышать ограничение длины сообщения
     *
     * @param chatId Код чата с пользователем
     */
    private void sendAllGamesMessage(long chatId) {
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
                        log.info("Отправлено сообщение с частью списка всех игр " + chatId);
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
            log.info("Отправлено сообщение со списком всех игр " + chatId);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Метод для отправки списка игр, подходящих под запрос поиска
     *
     * @param chatId      Код чата с пользователем
     * @param messageText Запрос поиска (название/часть названия игры)
     */
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
            log.info("Отправлено сообщение с ответом о поиске игры" + " '" + messageText + "' " + chatId);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}