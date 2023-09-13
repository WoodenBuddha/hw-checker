package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UnsupportedFilenameException;

public interface MessageRequestService {
    IncomingMessage determineRequestType(IncomingMessage incomingMessage) throws UnsupportedFilenameException;

}
