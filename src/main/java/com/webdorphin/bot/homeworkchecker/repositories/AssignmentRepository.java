package com.webdorphin.bot.homeworkchecker.repositories;

import com.webdorphin.bot.homeworkchecker.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByUserIdAndTaskCodeAndGrade(Long userId, String taskCode, Double grade);
    List<Assignment> findByUserIdAndGradeAndTaskCodeStartsWith(Long userId, Double grade, String taskCode);
    List<Assignment> findByUserIdAndGradeNotAndTaskCodeStartsWith(Long userId, Double grade, String taskCode);
}
