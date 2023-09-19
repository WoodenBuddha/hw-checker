package com.webdorphin.bot.homeworkchecker.telegram;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UnsupportedFilenameException;
import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.User;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.MessageRequestService;
import com.webdorphin.bot.homeworkchecker.services.UserService;
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
    private final UserService userService;

    @Autowired
    public TelegramBotServiceImpl(TelegramBotConfig telegramBotConfig,
                                  List<Processor> processors,
                                  MessageRequestService messageRequestService,
                                  UserService userService) {
        super(telegramBotConfig.getBotToken());
        this.telegramBotConfig = telegramBotConfig;
        this.processors = processors;
        this.messageRequestService = messageRequestService;
        this.userService = userService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var tgUser = Optional.of(update)
                .map(Update::getMessage)
                .map(Message::getFrom)
                .orElseThrow();

        try {
            var user = userService.find(tgUser);
            receiveMessage(map(update, user));

        } catch (UserNotFoundException e) {
            var sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Я не могу Вам помочь или пообщаться, т.к. я вас не знаю. Обратитесь к @WoodenBuddha");
            sendReply(sendMessage);
        } catch (UnsupportedFilenameException e) {
            var sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Некорректное имя файла. Файл должен называться по номеру задачи, например \"201.txt\"");
            sendReply(sendMessage);
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public void receiveMessage(IncomingMessage incomingMessage) {
        processors.stream()
                .filter(processor -> processor.canHandle(incomingMessage))
                .findFirst()
                .map(processor -> processor.process(incomingMessage))
                .orElseThrow();
    }


    public Message sendReply(SendMessage messageToSend) {
        try {
            return execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error("Couldn't send message to ");
        }
        return null;
    }

    public File downloadFileInfo(GetFile getFile) {
        try {
            return execute(getFile);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IncomingMessage map(Update update, User user) throws UnsupportedFilenameException {
        var incomingMessage = new IncomingMessage();
        incomingMessage.setUser(user);
        incomingMessage.setMessage(update.getMessage());
        incomingMessage.setRetrieveDocumentCallback(this::downloadFileInfo);
        incomingMessage.setSendReplyCallback(this::sendReply);
        return messageRequestService.determineRequestType(incomingMessage);
    }
}
