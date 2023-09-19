package com.webdorphin.bot.homeworkchecker.processors.impl;

import com.webdorphin.bot.homeworkchecker.dto.RequestType;
import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.processors.Processor;
import com.webdorphin.bot.homeworkchecker.services.HomeworkService;
import com.webdorphin.bot.homeworkchecker.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Random;
import java.util.function.Function;

@Service
@Slf4j
public class AdminMessageProcessorImpl implements Processor {

    private static final RequestType SUPPORTED_TYPE = RequestType.ADMIN_COMMAND;
    private static final String DELIMITER = ":";
    private static final String GRADES_COMMAND = "grades";
    private static final String RANDOM_PASSED_ASSIGNMENT_COMMAND = "random";

    private final HomeworkService homeworkService;
    private final UserService userService;

    @Autowired
    public AdminMessageProcessorImpl(HomeworkService homeworkService, UserService userService) {
        this.homeworkService = homeworkService;
        this.userService = userService;
    }

    @Override
    public boolean canHandle(IncomingMessage incomingMessage) {
        return SUPPORTED_TYPE.equals(incomingMessage.getRequestType());
    }

    @Override
    @Async
    public boolean process(IncomingMessage incomingMessage) {
        var adminChat = incomingMessage.getMessage().getChatId();
        var text = incomingMessage.getMessage().getText();
        var command = text.split(DELIMITER);
        if (command.length < 3) {
            incomingMessage.getSendReplyCallback().apply(buildMessage(adminChat, "Длина комманды меньше 3"));
        }

        // command:grades:username:week || command:random:username:week
        if (GRADES_COMMAND.equals(command[1])) {
            showUserGrades(command[2], command[3], incomingMessage.getSendReplyCallback(), adminChat);
        } else if (RANDOM_PASSED_ASSIGNMENT_COMMAND.equals(command[1])) {
            showRandomGradedAssignment(command[2], command[3], incomingMessage.getSendReplyCallback(), adminChat);
        }

        return false;
    }

    private void showUserGrades(String username, String week, Function<SendMessage, Message> callback, Long chatId) {
        try {
            var user = userService.find(username);
            var assignments = homeworkService.getGradedUserWeeklyAssignments(user.getId(), week);

            var sb = new StringBuilder();
            var total = 0.0;
            for (var a : assignments.entrySet()) {
                sb.append("Оценка за задачу ")
                        .append(a.getValue().getTaskCode())
                        .append(": ")
                        .append(a.getValue().getGrade())
                        .append("\n");
                total += a.getValue().getGrade();
            }

            sb.append("\nОбщая оценка за неделю: " + total);

            callback.apply(buildMessage(chatId, sb.toString()));

        } catch (UserNotFoundException e) {
            callback.apply(buildMessage(chatId, "Юзер с ником " + username + " не найден"));
        }
    }

    private void showRandomGradedAssignment(String username, String week, Function<SendMessage, Message> callback, Long chatId) {
        try {
            var r = new Random();
            var user = userService.find(username);
            var assignments = homeworkService.getGradedUserWeeklyAssignments(user.getId(), week);
            var list = assignments.values().stream().toList();
            var randomAssignment = list.get(r.nextInt(list.size()));

            var sb = new StringBuilder();
            sb.append("Задача ")
                    .append(randomAssignment.getTaskCode())
                    .append(":\n\n")
                    .append(randomAssignment.getSourceCode());

            callback.apply(buildMessage(chatId, sb.toString()));

        } catch (UserNotFoundException e) {
            callback.apply(buildMessage(chatId, "Юзер с ником " + username + " не найден"));
        } catch (Exception e) {
            callback.apply(buildMessage(chatId, "Чет сломалось: " + e.getMessage()));
        }
    }

    private SendMessage buildMessage(Long chatId, String text) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }
}
