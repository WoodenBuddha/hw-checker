package com.webdorphin.bot.homeworkchecker.dto.api;

import lombok.Data;

@Data
public class CreateTestCaseRequest {
    private String taskCode;
    private String input;
    private String output;
    private Integer variation;
}
