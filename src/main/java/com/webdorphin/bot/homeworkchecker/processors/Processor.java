package com.webdorphin.bot.homeworkchecker.processors;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;

public interface Processor {

    boolean canHandle(IncomingMessage incomingMessage);

    boolean process(IncomingMessage incomingMessage);
}
