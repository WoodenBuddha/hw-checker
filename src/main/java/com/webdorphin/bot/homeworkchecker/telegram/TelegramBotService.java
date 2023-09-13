package com.webdorphin.bot.homeworkchecker.telegram;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;

public interface TelegramBotService {

    void receiveMessage(IncomingMessage incomingMessage);

    void sendReply(Long chatId, String message);
}
