package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.User;
import com.webdorphin.bot.homeworkchecker.repositories.UserRepository;
import com.webdorphin.bot.homeworkchecker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User find(org.telegram.telegrambots.meta.api.objects.User telegramUser) throws UserNotFoundException {
        var username = telegramUser.getUserName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found by username=" + username));
    }
}
