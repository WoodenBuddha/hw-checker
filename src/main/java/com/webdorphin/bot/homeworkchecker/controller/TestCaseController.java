package com.webdorphin.bot.homeworkchecker.controller;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateTestCaseRequest;
import com.webdorphin.bot.homeworkchecker.model.TestCase;
import com.webdorphin.bot.homeworkchecker.repositories.TestCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testCases")
public class TestCaseController {

    private final TestCaseRepository testCaseRepository;

    public TestCaseController(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createTestCase(@RequestBody CreateTestCaseRequest createTestCaseRequest) {
        var testCase = new TestCase();
        testCase.setTaskCode(createTestCaseRequest.getTaskCode());
        testCase.setInput(createTestCaseRequest.getInput());
        testCase.setOutput(createTestCaseRequest.getOutput());
        testCase.setVariation(createTestCaseRequest.getVariation());

        testCaseRepository.save(testCase);

        return ResponseEntity.ok().build();
    }
}
