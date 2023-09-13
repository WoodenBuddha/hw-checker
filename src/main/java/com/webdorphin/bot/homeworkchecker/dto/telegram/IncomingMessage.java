package com.webdorphin.bot.homeworkchecker.dto.telegram;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.model.User;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.Function;

@Data
public class IncomingMessage {
    private User user;
    private boolean isAdmin;
    private boolean isVerifiedUser;
    private Message message;
    private RequestType requestType;
    private Function<GetFile, File> retrieveDocumentCallback;
    private Function<SendMessage, Message> sendReplyCallback;
}
