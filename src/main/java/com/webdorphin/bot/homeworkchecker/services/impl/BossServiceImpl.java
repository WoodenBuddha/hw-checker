package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;
import com.webdorphin.bot.homeworkchecker.repositories.UserRepository;
import com.webdorphin.bot.homeworkchecker.services.BossService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//@Service
@Slf4j
public class BossServiceImpl implements BossService {

    private static final String SHOW_STUDENT_GRADES_COMMAND = "grades|";

    private final UserRepository userRepository;

    public BossServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void executeBossCommand(IncomingMessage incomingMessage) {
        var command = incomingMessage.getMessage().getText();
        if (command.startsWith(SHOW_STUDENT_GRADES_COMMAND)) {

        }
    }
}
