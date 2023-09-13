package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.User;

public interface UserService {

    User find(org.telegram.telegrambots.meta.api.objects.User user) throws UserNotFoundException;

}
