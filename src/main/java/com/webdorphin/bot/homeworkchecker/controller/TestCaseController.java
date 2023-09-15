package com.webdorphin.bot.homeworkchecker.controller;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateTestCaseRequest;
import com.webdorphin.bot.homeworkchecker.dto.TestCaseDto;
import com.webdorphin.bot.homeworkchecker.model.TestCase;
import com.webdorphin.bot.homeworkchecker.repositories.TestCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/testCases")
public class TestCaseController {

    private final TestCaseRepository testCaseRepository;

    public TestCaseController(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createTestCase(@RequestBody CreateTestCaseRequest createTestCaseRequest) {
        var testCases = Optional.of(createTestCaseRequest)
                .map(CreateTestCaseRequest::getTestCases)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
        if (!testCases.isEmpty())
            testCaseRepository.saveAll(testCases);

        return ResponseEntity.ok().build();
    }

    private TestCase map(TestCaseDto dto) {
        var testCase = new TestCase();
        testCase.setTaskCode(dto.getTaskCode());
        testCase.setInput(dto.getInput());
        testCase.setOutput(dto.getOutput());
        testCase.setVariation(dto.getVariation());
        return testCase;
    }
}
