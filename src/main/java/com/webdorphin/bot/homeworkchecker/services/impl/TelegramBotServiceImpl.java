package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TelegramBotServiceImpl extends TelegramLongPollingBot implements TelegramBotService {

    private final TelegramBotConfig telegramBotConfig;
    private final List<Processor> processors;

    @Autowired
    public TelegramBotServiceImpl(TelegramBotConfig telegramBotConfig, List<Processor> processors) {
        super(telegramBotConfig.getBotToken());
        this.telegramBotConfig = telegramBotConfig;
        this.processors = processors;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var user = Optional.of(update)
                .map(Update::getMessage)
                .map(Message::getFrom)
                .orElseThrow();

    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public void receiveMessage() {

        var incomingMessage = new IncomingMessage();
        var outgoingMessage = processors.stream()
                .filter(processor -> processor.canHandle(incomingMessage))
                .findFirst()
        .map(processor -> processor.process(incomingMessage))
        .orElseThrow(() -> new RuntimeException("No processor is found that can handle a message"));

    }

    @Override
    public void sendMessage(Long chatId, String message) {

    }
}
