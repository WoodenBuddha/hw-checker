package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;

public interface MessageRequestService {
    IncomingMessage determineRequestType(IncomingMessage incomingMessage);

}
