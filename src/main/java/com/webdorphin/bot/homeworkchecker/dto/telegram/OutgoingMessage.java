package com.webdorphin.bot.homeworkchecker.dto.telegram;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Message;

@Data
public class OutgoingMessage {
    private Message initialMessage;
    private String text;
    private RequestType requestType;
    private String username;
}
