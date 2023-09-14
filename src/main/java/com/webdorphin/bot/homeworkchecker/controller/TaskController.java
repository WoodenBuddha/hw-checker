package com.webdorphin.bot.homeworkchecker.controller;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateTaskRequest;
import com.webdorphin.bot.homeworkchecker.model.Task;
import com.webdorphin.bot.homeworkchecker.repositories.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createTask(@RequestBody CreateTaskRequest createTaskRequest) {

        var task = new Task();
        task.setCode(createTaskRequest.getCode());
        task.setDeadline(createTaskRequest.getDeadline());
        task.setScore(createTaskRequest.getScore());

        taskRepository.save(task);

        return ResponseEntity.ok().build();
    }

}
