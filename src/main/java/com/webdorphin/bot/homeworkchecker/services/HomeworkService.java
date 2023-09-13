package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;

public interface HomeworkService {

    OutgoingMessage checkHomework(IncomingMessage incomingMessage);

}
