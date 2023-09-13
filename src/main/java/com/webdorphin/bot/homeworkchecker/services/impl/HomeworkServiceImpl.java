package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.HttpClientService;
import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionRequest;
import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionResponse;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.dto.telegram.OutgoingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.CodeXResponseError;
import com.webdorphin.bot.homeworkchecker.exceptions.NoTestCaseForAssignmentException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.model.TestCase;
import com.webdorphin.bot.homeworkchecker.repositories.AssignmentRepository;
import com.webdorphin.bot.homeworkchecker.repositories.TestCaseRepository;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@Slf4j
public class HomeworkServiceImpl implements HomeworkService {

    private static final String CPP_LANGUAGE = "cpp";
    private static final String TEST_CASE_INPUT_DELIMITER = ",";
    private static final String NEXT_LINE = "\n";

    private static final ConcurrentHashMap<String, List<TestCase>> TEST_CASES = new ConcurrentHashMap<>();

    private TelegramBotConfig botConfig;
    private final AssignmentRepository assignmentRepository;
    private final HttpClientService httpClientService;
    private final TestCaseRepository testCaseRepository;

    @Autowired
    public HomeworkServiceImpl(TelegramBotConfig botConfig,
                               AssignmentRepository assignmentRepository,
                               HttpClientService httpClientService,
                               TestCaseRepository testCaseRepository) {
        this.botConfig = botConfig;
        this.assignmentRepository = assignmentRepository;
        this.httpClientService = httpClientService;
        this.testCaseRepository = testCaseRepository;
    }

    @PostConstruct
    private void populateTestCases() {
        var loadedTestCases = testCaseRepository.findAll();
        for (TestCase tc : loadedTestCases) {
            var cachedTestCases = TEST_CASES.getOrDefault(tc.getTaskCode(), new ArrayList<>());
            cachedTestCases.add(tc);
            TEST_CASES.put(tc.getTaskCode(), cachedTestCases);
        }
    }

    @Override
    public Assignment checkHomework(Assignment assignment) {
        try {
            assignment = assignmentRepository.save(assignment);

            if(!testAssignment(assignment)) {
                assignment.setGrade(0.0);
            }
            assignment.setStatus(AssignmentStatus.GRADED);
            assignmentRepository.save(assignment);
        } catch (CodeXResponseError codeXResponseError) {
            assignment.setStatus(AssignmentStatus.ERROR);
            assignmentRepository.save(assignment);
            log.error("No response from remote!");
        } catch (NoTestCaseForAssignmentException e) {
            log.error("No test cases for task!");
            assignment.setStatus(AssignmentStatus.ERROR);
            assignmentRepository.save(assignment);
        }

        return assignment;
    }

    private boolean testAssignment(Assignment assignment) throws NoTestCaseForAssignmentException, CodeXResponseError {
        boolean allCasesPassed = true;
        var taskCode = assignment.getTaskCode();
        var testCases = TEST_CASES.get(taskCode);
        if (testCases == null) {
            testCases = testCaseRepository.findAllByTaskCode(taskCode);
            if (testCases == null || testCases.isEmpty()) {
                throw new NoTestCaseForAssignmentException("No test cases for task=" + taskCode);
            } else {
                TEST_CASES.put(taskCode, testCases);
            }
        }

        for (var testCase : testCases) {
            var response = runAssignmentRemotely(assignment, testCase);

            // TODO: can be removed if add test cases with trim via controller
            var preformatExpected = formatAndTrim(testCase.getOutput());
            var preformatActual = formatAndTrim(response.getOutput());

            var expectedResult = response.getError().isEmpty()
                    && preformatExpected.equalsIgnoreCase(preformatActual);
            if (!expectedResult) {
                assignment.setTestCaseError(testCase.getOutput());
                assignment.setErrorMsg(response.getError());
                allCasesPassed = false;
                break;
            }
        }

        if (allCasesPassed) {
            // TODO: refactor
            assignment.setGrade(testCases.get(0).getWeight());
        } else {
            assignment.setGrade(0.0);
        }
        return allCasesPassed;
    }

    private CodeXExecutionResponse runAssignmentRemotely(Assignment assignment, TestCase testCase) throws CodeXResponseError {
        var codeXRequest = new CodeXExecutionRequest();
        codeXRequest.setCode(assignment.getSourceCode());
        codeXRequest.setInput(buildCodeXInput(testCase));
        codeXRequest.setLanguage(CPP_LANGUAGE);

        var response = httpClientService.executeRemotely(codeXRequest);
        if (response == null)
            throw new CodeXResponseError("No response from remote executor");
        return response;
    }

    private String buildCodeXInput(TestCase testCase) {
        StringBuilder sb = new StringBuilder();
        if (!Optional.ofNullable(testCase)
                .map(TestCase::getInput)
                .map(String::isEmpty)
                .orElse(true))
            for (String in : testCase.getInput().split(TEST_CASE_INPUT_DELIMITER)) {
                sb.append(in).append(NEXT_LINE);
            }
        return sb.toString();
    }

    private String formatAndTrim(String msg) {
        return msg.replaceAll("\\s", "");
    }
}
