package com.webdorphin.bot.homeworkchecker.exceptions.task;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SubmissionAfterDeadlineException extends Exception {
    private String taskCode;
    private String username;
    private LocalDateTime deadline;
    private LocalDateTime currentTime;

    public SubmissionAfterDeadlineException(String string) {
        super(string);
    }

    public SubmissionAfterDeadlineException(String string, String taskCode, String username, LocalDateTime deadline, LocalDateTime currentTime) {
        super(string);
        this.taskCode = taskCode;
        this.currentTime = currentTime;
        this.deadline = deadline;
        this.username = username;
    }
}
