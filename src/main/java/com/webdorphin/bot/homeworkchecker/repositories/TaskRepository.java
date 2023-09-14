package com.webdorphin.bot.homeworkchecker.repositories;

import com.webdorphin.bot.homeworkchecker.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByCode(String code);

}
