package com.webdorphin.bot.homeworkchecker.dto.remote;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeXExecutionResponse {
    private String timeStamp;
    private Integer status;
    private String output;
    private String error;
    private String language;
    private String info;
}
