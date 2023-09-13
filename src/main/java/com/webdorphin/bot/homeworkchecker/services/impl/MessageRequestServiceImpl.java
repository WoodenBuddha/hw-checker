package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UnsupportedFilenameException;
import com.webdorphin.bot.homeworkchecker.services.MessageRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MessageRequestServiceImpl implements MessageRequestService {

    private static final Set<String> POSSIBLE_GRADE_REQUEST_TEXT = Set.of("оценка", "оценки", "grade", "grades");
    private static final Set<String> ALLOWED_FILENAMES = Set.of("all");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("\\d+");

    @Override
    public IncomingMessage determineRequestType(IncomingMessage incomingMessage) throws UnsupportedFilenameException {
        tryToDetermineRequestType(incomingMessage);
        return incomingMessage;
    }

    private void tryToDetermineRequestType(IncomingMessage incomingMessage) throws UnsupportedFilenameException {
        if (isGradeRequest(incomingMessage)) {
            incomingMessage.setRequestType(RequestType.REQUEST_GRADE);
        } else if (isUploadHomework(incomingMessage)) {
            var fileName = incomingMessage.getMessage().getDocument().getFileName();
            if (!ALLOWED_FILENAMES.contains(fileName)
                || !FILENAME_PATTERN.matcher(fileName).matches()) {
                throw new UnsupportedFilenameException(fileName);
            }

            incomingMessage.setRequestType(RequestType.UPLOAD_HOMEWORK);
        } else {
            incomingMessage.setRequestType(RequestType.NOT_DETERMINED);
            log.warn("Couldn't determine request from {}", incomingMessage.getUser().getUsername());
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
