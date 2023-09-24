package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.exceptions.task.SubmissionAfterDeadlineException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.TaskNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;
import com.webdorphin.bot.homeworkchecker.model.Task;

import java.util.List;
import java.util.Map;

public interface HomeworkService {
    @Deprecated(forRemoval = true)
    Assignment checkHomework(Assignment assignment, String username) throws TaskNotFoundException, SubmissionAfterDeadlineException;
    Assignment checkHomework(Assignment assignment, String username, Task task) throws TaskNotFoundException, SubmissionAfterDeadlineException;
    Map<String, Assignment> getGradedUserWeeklyAssignments(Long userId, String week);
}
