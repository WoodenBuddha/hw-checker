package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UnsupportedFilenameException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.SubmissionAfterDeadlineException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.TaskNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.model.User;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.repositories.TaskRepository;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import jakarta.transaction.Transactional;
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
import java.time.LocalDateTime;
import java.util.function.Function;

import static com.webdorphin.bot.homeworkchecker.services.impl.MessageRequestServiceImpl.ALLOWED_FILENAMES;
import static com.webdorphin.bot.homeworkchecker.util.FileUtils.removeExtension;

@Service
@Slf4j
public class UploadHomeworkRequestProcessorImpl implements Processor {
    private static final RequestType SUPPORTED_TYPE = RequestType.UPLOAD_HOMEWORK;

    private final TelegramBotConfig botConfig;
    private final HomeworkService homeworkService;
    private final TaskRepository taskRepository;

    private static final String TG_ERROR = "Проблема с телегой, попробуйте мб попзже";
    private static final String ALL_IN_ONE_CHECK_NOT_SUPPORTED = "Пока что проверка заданий в одном файле не поддерживается";
    private static final String TASK_DOES_NOT_EXIST = "Задачи с номером %s не существует";
    private static final String DEADLINE_TASK = "Дедлайн по задаче %s прошел %s";
    private static final String CRITICAL_ERROR_MSG = "Чет пошло не так...\nВот ошибка: %s\n]\nПередайте преподавателю @WoodenBuddha, пусть разберется";

    @Autowired
    private UploadHomeworkRequestProcessorImpl(HomeworkService homeworkService, TelegramBotConfig botConfig, TaskRepository taskRepository) {
        this.homeworkService = homeworkService;
        this.botConfig = botConfig;
        this.taskRepository = taskRepository;
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

            var sourceCode = getSubmittedAssignment(document, incomingMessage.getRetrieveDocumentCallback());
            var taskCode = removeExtension(document.getFileName());

            var assignment = new Assignment();
            assignment.setStatus(AssignmentStatus.NEW);
            assignment.setSourceCode(sourceCode);
            assignment.setTaskCode(taskCode);

            var username = incomingMessage.getUser().getUsername();

            var task = taskRepository.findByCode(taskCode)
                    .orElseThrow(() -> new TaskNotFoundException("Could find task " + taskCode + " sent by " + username, taskCode, username));
            if (task.getDeadline().isBefore(LocalDateTime.now()))
                throw new SubmissionAfterDeadlineException("Task is sent after deadline", taskCode, username, task.getDeadline(), LocalDateTime.now());

            var result = homeworkService.checkHomework(assignment, username, task);
            sendMessageBack(incomingMessage, buildResultingText(result, taskCode, task.getScore()));

        } catch (TelegramApiException e) {
            log.error("Something wrong with getting file info from tg! {}", e.getMessage());
            sendMessageBack(incomingMessage, TG_ERROR);
        } catch (IOException e) {
            log.error("Something wrong with getting file from tg server! {}", e.getMessage());
            sendMessageBack(incomingMessage, TG_ERROR);
        } catch (UnsupportedFilenameException e) {
            log.warn("All.txt");
            sendMessageBack(incomingMessage, ALL_IN_ONE_CHECK_NOT_SUPPORTED);
        } catch (TaskNotFoundException e) {
            log.error("No such task = " + e.getTaskCode());
            sendMessageBack(incomingMessage, TASK_DOES_NOT_EXIST.formatted(e.getTaskCode()));
        } catch (SubmissionAfterDeadlineException e) {
            sendMessageBack(incomingMessage, DEADLINE_TASK.formatted(e.getTaskCode(), e.getDeadline()));
        } catch (Throwable t) {
            log.error("Упали при проверке - ", t);
            sendMessageBack(incomingMessage, CRITICAL_ERROR_MSG.formatted(t.getMessage()));
        }

        return true;
    }

    private String getSubmittedAssignment(Document document, Function<GetFile, File> retriever) throws TelegramApiException, IOException {
        var file = getDocument(document, retriever);
        return downloadFile(file);
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

    private String buildResultingText(Assignment result, String taskCode, Double maxScore) {
        var resultMsg = "Ваша оценка за задание " + taskCode + ": " + result.getGrade() + " из " + maxScore;

        if (result.getGrade() == 0.0) {
            resultMsg = result.getErrorMsg() != null && !result.getErrorMsg().isEmpty()
                    ? resultMsg + "\nОшибка: " + result.getErrorMsg()
                    : resultMsg;
            resultMsg += "\nНа экран нужно вывести: " + result.getTestCaseError();
        }

        return resultMsg;
    }

    private void sendMessageBack(IncomingMessage incomingMessage, String text) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(incomingMessage.getMessage().getChatId());
        sendMessage.setText(text);
        incomingMessage.getSendReplyCallback().apply(sendMessage);
    }
}
