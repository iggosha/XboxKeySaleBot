package com.xboxkeysale.bot.config;

import com.xboxkeysale.bot.XKSBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Класс для инициализации бота
 *
 * @author Igor Golovkov
 */
@Slf4j
@Component
public class Initializer {

    /**
     * Бин бота
     */
    @Autowired
    XKSBot xksBot;

    /**
     * Инициализация и подключение бота
     */
    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(xksBot);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}