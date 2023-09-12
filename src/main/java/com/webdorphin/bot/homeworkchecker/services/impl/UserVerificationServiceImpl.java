package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.services.UserVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Slf4j
public class UserVerificationServiceImpl implements UserVerificationService {


    @Override
    public void verify(User user) {

    }
}
