package com.webdorphin.bot.homeworkchecker.repositories;

import com.webdorphin.bot.homeworkchecker.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByCode(String code);

}
