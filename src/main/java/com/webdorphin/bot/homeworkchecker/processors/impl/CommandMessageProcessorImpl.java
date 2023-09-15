package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Function;

@Service
@Slf4j
public class CommandMessageProcessorImpl implements Processor {

    private static final RequestType SUPPORTED_TYPE = RequestType.COMMAND;
    private static final String START = "/start";
    private static final String HELP = "/help";

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    public boolean process(IncomingMessage incomingMessage) {
        var text = incomingMessage.getMessage().getText();
        if (START.equals(text)) {
            sendMessage("Хай!\nЧто я могу тделать?",
                    incomingMessage.getUser().getChatId(),
                    incomingMessage.getSendReplyCallback());
        } else if (HELP.equals(text)) {
            sendMessage("Чтобы я оценил домашку, просто отправьте мне текстовый файл с названием задачи. Например \"201.txt\"\nЧтобы посмотреть свои текущие оценки, напишите \"оценки\"",
                    incomingMessage.getUser().getChatId(),
                    incomingMessage.getSendReplyCallback());
        }

        return true;
    }

    private void sendMessage(String msg, Long chatId, Function<SendMessage, Message> callback) {
        var sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(chatId);
        callback.apply(sendMessage);
    }
}
