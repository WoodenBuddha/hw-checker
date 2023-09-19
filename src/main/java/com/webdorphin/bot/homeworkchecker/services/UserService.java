package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateUserRequest;
import com.webdorphin.bot.homeworkchecker.exceptions.GroupNotFoundException;
import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.User;

public interface UserService {

    User find(org.telegram.telegrambots.meta.api.objects.User user) throws UserNotFoundException;
    User find(String username) throws UserNotFoundException;
    User createUser(CreateUserRequest createUserRequest) throws GroupNotFoundException;

}
