package com.webdorphin.bot.homeworkchecker.dto.remote;

import lombok.Data;

@Data
public class CodeXExecutionRequest {
    private String code;
    private String language;
    private String input;
}
