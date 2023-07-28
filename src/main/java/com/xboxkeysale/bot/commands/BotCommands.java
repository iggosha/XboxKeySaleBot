package com.xboxkeysale.bot.commands;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

/**
 * Класс для кнопок бота со стандартными сообщениями
 *
 * @author Igor Golovkov
 */
public interface BotCommands {

    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "start bot"),
            new BotCommand("/help", "bot info")
    );

    String HELP_TEXT = """
            Пиши *Название_игры* для поиска игры и её цены
            - Название писать на английском (как в оригинале)
            - Можно искать по первым словам/буквам - будут предложены возможные варианты
            - Регистр не важен, можно без с/без заглавной
                        
            Пиши "Цены" - для просмотра информации обо всех имеющихся играх и их ценах
            Подписывайся на наш канал в Telegram с обновлениями каталога - @XboxKeySale
            """;

    String START_TEXT = """
            Привет, я бот канала @XboxKeySale! Подпишись, чтобы быть в курсе всех обновлений каталога.
            Я могу помочь найти интересующую тебя игру или показать каталог всех игр!
            - Пиши "/help" для получения справочной информации
            - Пиши *Название игры* для поиска игры и её цены
            - Пиши "Цены" - для просмотра информации обо всех имеющихся играх и их ценах (их ОЧЕНЬ много!)
            """;
}
