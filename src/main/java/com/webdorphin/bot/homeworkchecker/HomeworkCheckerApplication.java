package com.webdorphin.bot.homeworkchecker;

import com.webdorphin.bot.homeworkchecker.services.impl.TelegramBotServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class HomeworkCheckerApplication {

    public static void main(String[] args) throws TelegramApiException {
        var ctx = SpringApplication.run(HomeworkCheckerApplication.class, args);
        var telegramBot = ctx.getBean(TelegramBotServiceImpl.class);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBot);
    }
}
