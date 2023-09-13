package com.webdorphin.bot.homeworkchecker.telegram;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.MessageRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TelegramBotServiceImpl extends TelegramLongPollingBot implements TelegramBotService {

    private final TelegramBotConfig telegramBotConfig;
    private final List<Processor> processors;
    private final MessageRequestService messageRequestService;

    @Autowired
    public TelegramBotServiceImpl(TelegramBotConfig telegramBotConfig,
                                  List<Processor> processors,
                                  MessageRequestService messageRequestService) {
        super(telegramBotConfig.getBotToken());
        this.telegramBotConfig = telegramBotConfig;
        this.processors = processors;
        this.messageRequestService = messageRequestService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var user = Optional.of(update)
                .map(Update::getMessage)
                .map(Message::getFrom)
                .orElseThrow();

        var incomingMessage = new IncomingMessage();
        incomingMessage.setUsername(user.getUserName());
        incomingMessage.setMessage(update.getMessage());
        incomingMessage.setRetrieveDocumentCallback(this::downloadFileInfo);
        incomingMessage = messageRequestService.determineRequestType(incomingMessage);
        receiveMessage(incomingMessage);
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public void receiveMessage(IncomingMessage incomingMessage) {
        var outgoingMessage = processors.stream()
                .filter(processor -> processor.canHandle(incomingMessage))
                .findFirst()
                .map(processor -> processor.process(incomingMessage))
                .orElseThrow();

        sendReply(outgoingMessage.getInitialMessage().getChatId(), outgoingMessage.getText());
    }

    @Override
    public void sendReply(Long chatId, String message) {
        var messageToSend = new SendMessage();
        messageToSend.setChatId(chatId);
        messageToSend.setText(message);

        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error("Couldn't send message to ");
        }
    }

    public File downloadFileInfo(GetFile getFile) {
        try {
            return execute(getFile);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
