package com.tuaev.utils_bot.config_app;

import com.tuaev.utils_bot.bot.UtilsBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Logger;

@Configuration
public class Config {

    private final Logger logger = Logger.getLogger(Config.class.getName());

    @Bean
    public TelegramBotsApi telegramBotsApi(UtilsBot bot){
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            logger.info("\nБот успешно зарегистрирован");
            return telegramBotsApi;
        }catch (TelegramApiException e){
            logger.info("Произошла ошибка: " + e.getMessage());
        }
        return null;
    }
}
