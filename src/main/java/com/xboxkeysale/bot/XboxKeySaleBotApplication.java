package com.xboxkeysale.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Класс для запуска приложения
 *
 * @author Igor Golovkov
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class XboxKeySaleBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(XboxKeySaleBotApplication.class, args);
    }

}
