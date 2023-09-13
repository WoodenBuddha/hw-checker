package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.services.MessageRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class MessageRequestServiceImpl implements MessageRequestService {

    private static final Set<String> POSSIBLE_GRADE_REQUEST_TEXT = Set.of("оценка", "оценки", "grade", "grades");

    @Override
    public IncomingMessage determineRequestType(IncomingMessage incomingMessage) {
        tryToDetermineRequestType(incomingMessage);
        return incomingMessage;
    }

    private void tryToDetermineRequestType(IncomingMessage incomingMessage) {
        if (isGradeRequest(incomingMessage)) {
            incomingMessage.setRequestType(RequestType.REQUEST_GRADE);
        } else if (isUploadHomework(incomingMessage)) {
            incomingMessage.setRequestType(RequestType.UPLOAD_HOMEWORK);
        } else {
            incomingMessage.setRequestType(RequestType.NOT_DETERMINED);
            log.warn("Couldn't determine request from {}", incomingMessage.getUsername());
        }
        log.debug("Determined type of the request is {} for msgId = {}", incomingMessage.getRequestType(), incomingMessage.getMessage().getMessageId());
    }

    private boolean isGradeRequest(IncomingMessage message) {
        return Optional.of(message)
                .map(IncomingMessage::getMessage)
                .map(Message::getText)
                .filter(msg -> !msg.isBlank())
                .map(String::toLowerCase)
                .map(POSSIBLE_GRADE_REQUEST_TEXT::contains)
                .orElse(false);
    }

    private boolean isUploadHomework(IncomingMessage incomingMessage) {
        return Optional.of(incomingMessage)
                .map(IncomingMessage::getMessage)
                .map(Message::hasDocument)
                .orElse(false);
    }
}
