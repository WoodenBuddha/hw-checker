package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StudentMessageProcessorImpl implements Processor {

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return incomingMessage.isVerifiedUser() && !incomingMessage.isAdmin();
    }

    @Override
    public OutgoingMessage process(IncomingMessage incomingMessage) {
        return null;
    }
}
