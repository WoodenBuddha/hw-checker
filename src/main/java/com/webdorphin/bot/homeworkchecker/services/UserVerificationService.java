package com.webdorphin.bot.homeworkchecker.services;

import org.telegram.telegrambots.meta.api.objects.User;

public interface UserVerificationService {

    void verify(User user);

}
