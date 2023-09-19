package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.model.User;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class GradeRetrieveRequestProcessorImpl implements Processor {

    private static final String TOTAL_GRADE_HEADER = "Текущая общая оценка за задачи: ";
    private static final String WEEK_GRADE_HEADER = "Оценка за %s неделю";
    private static final String EQ = ": ";
    private static final String NEW_LINE = "\n";

    private static final RequestType SUPPORTED_TYPE = RequestType.REQUEST_GRADE;
    private final UserRepository userRepository;

    public GradeRetrieveRequestProcessorImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    @Async
    @Transactional
    public boolean process(IncomingMessage incomingMessage) {
        processGradesRequest(incomingMessage.getUser().getUsername(),
                incomingMessage.getMessage().getChatId(),
                incomingMessage.getSendReplyCallback());
        return true;
    }

    private void processGradesRequest(String username,
                                      Long chatId,
                                      Function<SendMessage, Message> sendMessageCallback) {
        var userAssignments = userRepository.findByUsername(username)
                .map(User::getAssignments)
                .orElse(Collections.emptyList());

        var grades = collectGrades(userAssignments);
        var totalGrade = grades.values()
                    .stream()
                    .reduce(0.0, Double::sum);
        var text = buildGradesText(totalGrade, grades);

        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessageCallback.apply(sendMessage);

    }

    // todo: extract to service
    private Map<String, Double> collectGrades(List<Assignment> assignments) {
        var maxGradePerAssignment = new HashMap<String, Double>();
        for (var assignment : assignments) {
            var task = assignment.getTaskCode();
            if (maxGradePerAssignment.get(task) == null || maxGradePerAssignment.get(task) < assignment.getGrade()) {
                maxGradePerAssignment.put(task, assignment.getGrade());
            }
        }
        return maxGradePerAssignment;
    }

    private String buildGradesText(Double total, Map<String, Double> grades) {
        var sb = new StringBuilder();
        sb.append(TOTAL_GRADE_HEADER)
                .append(String.format("%.2f", total))
                .append(NEW_LINE)
                .append(NEW_LINE);

        Map<String, Double> weeklyGrades = new HashMap<>();
        grades.forEach((key, value) -> {
            var week = key.substring(0, 1);
            weeklyGrades.put(week, weeklyGrades.getOrDefault(week, 0.0) + value);
        });

        weeklyGrades.forEach((k,v) -> {
            addWeekGrade(k,v,sb);
        });

        return sb.toString();
    }

    private void addWeekGrade(String weekNum, Double grade, StringBuilder sb) {
        sb.append(WEEK_GRADE_HEADER.formatted(weekNum))
                .append(EQ)
                .append(String.format("%.2f", grade))
                .append(NEW_LINE)
                .append(NEW_LINE);
    }

}
