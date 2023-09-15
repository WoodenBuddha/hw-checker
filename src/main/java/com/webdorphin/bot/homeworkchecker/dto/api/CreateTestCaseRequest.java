package com.webdorphin.bot.homeworkchecker.dto.api;

import com.webdorphin.bot.homeworkchecker.dto.TestCaseDto;
import lombok.Data;

import java.util.List;

@Data
public class CreateTestCaseRequest {
    List<TestCaseDto> testCases;
}
