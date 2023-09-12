package com.webdorphin.bot.homeworkchecker.dto;

import lombok.Data;

@Data
public class ProgramExecutionDto {
    private String sourceCode;
    private String language;
    private String[] inputParams;
}
