package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UploadHomeworkRequestProcessorImpl implements Processor {
    private static final RequestType SUPPORTED_TYPE = RequestType.UPLOAD_HOMEWORK;

    private final HomeworkService homeworkService;

    @Autowired
    private UploadHomeworkRequestProcessorImpl(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    @Async
    public boolean process(IncomingMessage incomingMessage) {
        var result = homeworkService.checkHomework(incomingMessage);

        return true;
    }
}
