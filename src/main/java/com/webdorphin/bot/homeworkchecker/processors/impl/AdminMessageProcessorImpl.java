package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminMessageProcessorImpl implements Processor {

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return false;
    }

    @Override
    public boolean process(IncomingMessage incomingMessage) {
        return false;
    }
}
