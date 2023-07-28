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

@Slf4j
@Component
public class XKSBot extends TelegramLongPollingBot implements BotCommands {

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

    private void botAnswerUtils(String receivedMessage, long chatId) {
        switch (receivedMessage) {
            case "/start" -> sendDefaultMessage(chatId, START_TEXT);
            case "/help" -> sendDefaultMessage(chatId, HELP_TEXT);
            case "цены" -> sendAllGamesMessage(chatId);
            default -> findGameSendMessage(chatId, receivedMessage);
        }
    }

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