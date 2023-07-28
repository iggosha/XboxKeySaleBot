package com.xboxkeysale.bot.commands;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/start", "start bot"),
            new BotCommand("/help", "bot info")
    );

    String HELP_TEXT = """
            *Название_игры* - поиск игры и цены
            Писать на английском (как в оригинале). Можно искать по первым словам/буквам - будут предложены возможные варианты]
            "Цены" - информация обо всех имеющихся играх и их ценах
            Наш канал в Telegram с новостями - @XboxKeySale
            """;

    String START_TEXT = """
            "/help" - для получения справочной информации
            *Название_игры* - поиск игры и цены
            "Цены" - информация обо всех имеющихся играх и их ценах
            Наш канал в Telegram с новостями - @XboxKeySale
            """;
}
