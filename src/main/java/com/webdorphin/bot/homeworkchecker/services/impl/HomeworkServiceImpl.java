package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.HttpClientService;
import com.webdorphin.bot.homeworkchecker.config.TelegramBotConfig;
import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionRequest;
import com.webdorphin.bot.homeworkchecker.dto.remote.CodeXExecutionResponse;
import com.webdorphin.bot.homeworkchecker.exceptions.CodeXResponseError;
import com.webdorphin.bot.homeworkchecker.exceptions.NoTestCaseForAssignmentException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.SubmissionAfterDeadlineException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.TaskNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.model.TestCase;
import com.webdorphin.bot.homeworkchecker.repositories.AssignmentRepository;
import com.webdorphin.bot.homeworkchecker.repositories.TaskRepository;
import com.webdorphin.bot.homeworkchecker.repositories.TestCaseRepository;
import com.webdorphin.bot.homeworkchecker.repositories.UserRepository;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class HomeworkServiceImpl implements HomeworkService {

    private static final String CPP_LANGUAGE = "cpp";
    private static final String TEST_CASE_INPUT_DELIMITER = ",";
    private static final String NEXT_LINE = "\n";

    private static final ConcurrentHashMap<String, List<TestCase>> TEST_CASES = new ConcurrentHashMap<>();

    private TelegramBotConfig botConfig;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final HttpClientService httpClientService;
    private final TestCaseRepository testCaseRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public HomeworkServiceImpl(TelegramBotConfig botConfig,
                               AssignmentRepository assignmentRepository,
                               HttpClientService httpClientService,
                               TestCaseRepository testCaseRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository) {
        this.botConfig = botConfig;
        this.assignmentRepository = assignmentRepository;
        this.httpClientService = httpClientService;
        this.testCaseRepository = testCaseRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
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
    @Transactional
    public Assignment checkHomework(Assignment assignment, String username) throws TaskNotFoundException, SubmissionAfterDeadlineException {
        try {
            var taskCode = assignment.getTaskCode();
            var task = taskRepository.findByCode(taskCode)
                    .orElseThrow(() -> new TaskNotFoundException("Could find task " + taskCode + " sent by " + username, taskCode, username));
            if (task.getDeadline().isBefore(LocalDateTime.now()))
                throw new SubmissionAfterDeadlineException("Task is sent after deadline", taskCode, username, task.getDeadline(), LocalDateTime.now());

            userRepository.findByUsername(username)
                    .ifPresent(assignment::setUser);
            assignment = assignmentRepository.save(assignment);

            if (!testAssignment(assignment, task.getScore())) {
                assignment.setGrade(0.0);
            }
            assignment.setStatus(AssignmentStatus.GRADED);
        } catch (CodeXResponseError codeXResponseError) {
            assignment.setStatus(AssignmentStatus.ERROR);
            log.error("No response from remote!");
        } catch (NoTestCaseForAssignmentException e) {
            log.error("No test cases for task!");
            assignment.setStatus(AssignmentStatus.ERROR);
        }
        assignmentRepository.save(assignment);

        return assignment;
    }

    private boolean testAssignment(Assignment assignment, Double maxScore) throws NoTestCaseForAssignmentException, CodeXResponseError {
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
            var preformatActual = postprocessOutput(response.getOutput());

            var expectedResult = response.getError().isEmpty()
                    && preformatExpected.equalsIgnoreCase(preformatActual);
            if (!expectedResult) {
                assignment.setTestCaseError(testCase.getOutputTemplate());
                assignment.setErrorMsg(response.getError());
                allCasesPassed = false;
                break;
            }
        }

        if (allCasesPassed) {
            assignment.setGrade(maxScore);
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

    private String postprocessOutput(String output) {
        var out = getLastLine(output);
        out = formatAndTrim(out);
        return out;
    }

    private String getLastLine(String text) {
        var lineBreakerIdx = text.lastIndexOf("\n");
        return lineBreakerIdx != -1
                ? text.substring(lineBreakerIdx)
                : text;
    }

    private String formatAndTrim(String msg) {
        return msg.replaceAll("\\s", "");
    }
}
