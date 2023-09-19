package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.exceptions.task.SubmissionAfterDeadlineException;
import com.webdorphin.bot.homeworkchecker.exceptions.task.TaskNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.Assignment;

import java.util.List;
import java.util.Map;

public interface HomeworkService {

    Assignment checkHomework(Assignment assignment, String username) throws TaskNotFoundException, SubmissionAfterDeadlineException;
    Map<String, Assignment> getGradedUserWeeklyAssignments(Long userId, String week);
}
