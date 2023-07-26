package com.xboxkeysale.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@NoArgsConstructor
@PropertySource("/application.properties")
public class BotConfig {
    @Value("${bot.username}") String name;
    @Value("${bot.token}") String token;
    @Value("${bot.chatId}") String chatId;
}
