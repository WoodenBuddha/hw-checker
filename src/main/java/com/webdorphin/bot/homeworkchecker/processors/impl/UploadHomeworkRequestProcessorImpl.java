package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UnsupportedFilenameException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static com.webdorphin.bot.homeworkchecker.services.impl.MessageRequestServiceImpl.ALLOWED_FILENAMES;
import static com.webdorphin.bot.homeworkchecker.util.FileUtils.removeExtension;

@Service
@Slf4j
public class UploadHomeworkRequestProcessorImpl implements Processor {
    private static final RequestType SUPPORTED_TYPE = RequestType.UPLOAD_HOMEWORK;

    private final TelegramBotConfig botConfig;
    private final HomeworkService homeworkService;

    @Autowired
    private UploadHomeworkRequestProcessorImpl(HomeworkService homeworkService, TelegramBotConfig botConfig) {
        this.homeworkService = homeworkService;
        this.botConfig = botConfig;
    }

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    @Async
    public boolean process(IncomingMessage incomingMessage) {
        try {
            var document = incomingMessage.getMessage().getDocument();
            if (ALLOWED_FILENAMES.contains(removeExtension(document.getFileName()))) {
                throw new UnsupportedFilenameException("Not supported yet");
            }

            var file = getDocument(document, incomingMessage.getRetrieveDocumentCallback());
            var sourceCode = downloadFile(file);
            var taskCode = removeExtension(document.getFileName());

            var assignment = new Assignment();
            assignment.setStatus(AssignmentStatus.NEW);
            assignment.setSourceCode(sourceCode);
            assignment.setTaskCode(taskCode);

            var result = homeworkService.checkHomework(assignment, incomingMessage.getUser().getUsername());
            var sendMessage = new SendMessage();
            sendMessage.setChatId(incomingMessage.getMessage().getChatId());
            sendMessage.setText(buildResultingText(result, taskCode));
            incomingMessage.getSendReplyCallback().apply(sendMessage);

        } catch (TelegramApiException e) {
            log.error("Something wrong with getting file info from tg! {}", e.getMessage());
            var sendMessage = new SendMessage();
            sendMessage.setChatId(incomingMessage.getMessage().getChatId());
            sendMessage.setText("Проблема с телегой, попробуйте мб попзже");
            incomingMessage.getSendReplyCallback().apply(sendMessage);
        } catch (IOException e) {
            log.error("Something wrong with getting file from tg server! {}", e.getMessage());
            var sendMessage = new SendMessage();
            sendMessage.setChatId(incomingMessage.getMessage().getChatId());
            sendMessage.setText("Проблема с телегой, попробуйте мб попзже");
            incomingMessage.getSendReplyCallback().apply(sendMessage);
        } catch (UnsupportedFilenameException e) {
            log.warn("All.txt");
            var sendMessage = new SendMessage();
            sendMessage.setChatId(incomingMessage.getMessage().getChatId());
            sendMessage.setText("Пока что проверка заданий в одном файле не поддерживается");
            incomingMessage.getSendReplyCallback().apply(sendMessage);
        }


        return true;
    }

    private File getDocument(Document document, Function<GetFile, File> callback) throws TelegramApiException {
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        return callback.apply(getFile);
    }

    private String downloadFile(File file) throws IOException {
        InputStream is = new URL(file.getFileUrl(botConfig.getBotToken())).openStream();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String buildResultingText(Assignment result, String taskCode) {
        var resultMsg = "Ваша оценка за задание " + taskCode + " = " + result.getGrade();

        if (result.getGrade() == 0.0) {
            resultMsg = result.getErrorMsg() != null && !result.getErrorMsg().isEmpty()
                    ? resultMsg + "\nОшибка: " + result.getErrorMsg()
                    : resultMsg;
            resultMsg += "\nНа экран нужно вывести: " + result.getTestCaseError();
        }


        return resultMsg;
    }
}
