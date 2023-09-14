package com.webdorphin.bot.homeworkchecker.dto.api;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {
    private String code;
    private LocalDateTime deadline;
    private Double score;
}
