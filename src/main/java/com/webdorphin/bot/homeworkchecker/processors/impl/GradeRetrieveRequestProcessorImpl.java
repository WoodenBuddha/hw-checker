package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GradeRetrieveRequestProcessorImpl implements Processor {

    private static final RequestType SUPPORTED_TYPE = RequestType.REQUEST_GRADE;

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return incomingMessage.isVerifiedUser()
                && SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    public boolean process(IncomingMessage incomingMessage) {


        return false;
    }
}
