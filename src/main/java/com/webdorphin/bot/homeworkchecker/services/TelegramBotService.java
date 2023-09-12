package com.webdorphin.bot.homeworkchecker.services;

public interface TelegramBotService {

    void receiveMessage();

    void sendMessage(Long chatId, String message);
}
