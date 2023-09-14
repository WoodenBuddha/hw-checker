package com.webdorphin.bot.homeworkchecker.exceptions.task;

import lombok.Getter;

@Getter
public class TaskNotFoundException extends Exception {
    private String taskCode;
    private String username;

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, String taskCode, String username) {
        super(message);
        this.taskCode = taskCode;
        this.username = username;
    }
}
