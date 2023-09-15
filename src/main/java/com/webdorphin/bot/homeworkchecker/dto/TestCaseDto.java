package com.webdorphin.bot.homeworkchecker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestCaseDto {
    @NotNull
    private String taskCode;
    private String input;
    private String output;
    private Integer variation;
    private String outputTemplate;
}
