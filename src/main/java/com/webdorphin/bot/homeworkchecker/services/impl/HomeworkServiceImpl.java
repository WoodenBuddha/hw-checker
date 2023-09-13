package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import com.webdorphin.bot.homeworkchecker.dto.remote.ProgramRemoteExecutionRequest;
import com.webdorphin.bot.homeworkchecker.dto.remote.ProgramRemoteExecutionResponse;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.repositories.AssignmentRepository;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
@Slf4j
public class HomeworkServiceImpl implements HomeworkService {

    private TelegramBotConfig botConfig;
    private final AssignmentRepository assignmentRepository;

    private static final String REMOTE_CPP_EXECUTION_ENDPOINT = "https://api.codex.jaagrav.in";
    private static final String CPP_LANGUAGE = "cpp";

    @Autowired
    public HomeworkServiceImpl(TelegramBotConfig botConfig, AssignmentRepository assignmentRepository) {
        this.botConfig = botConfig;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public OutgoingMessage checkHomework(IncomingMessage incomingMessage) {
        try {
            var file = getDocument(incomingMessage.getMessage().getDocument(), incomingMessage.getRetrieveDocumentCallback());
            var sourceCode = downloadFile(file);

            var assignment = new Assignment();
            assignment.setStatus(AssignmentStatus.NEW);
            assignment.setUser(incomingMessage.getUser());
            assignment.setSourceCode(sourceCode);
            assignment = assignmentRepository.save(assignment);

            var response = runAssignmentRemotely(assignment);

        } catch (TelegramApiException e) {
            log.error("Something wrong with getting file info from tg! {}", e.getMessage());
        } catch (IOException e) {
            log.error("Something wrong with getting file from tg server! {}", e.getMessage());
        }


        return new OutgoingMessage();
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

    private ProgramRemoteExecutionResponse runAssignmentRemotely(Assignment assignment) {
        RestTemplate restTemplate = new RestTemplate();

        var remoteRequest = new ProgramRemoteExecutionRequest();
        remoteRequest.setCode(assignment.getSourceCode());
        remoteRequest.setLanguage(CPP_LANGUAGE);
        remoteRequest.setInput("");

        HttpEntity<ProgramRemoteExecutionRequest> request = new HttpEntity<>(remoteRequest);
        return restTemplate.postForObject(REMOTE_CPP_EXECUTION_ENDPOINT, request, ProgramRemoteExecutionResponse.class);
    }
}
