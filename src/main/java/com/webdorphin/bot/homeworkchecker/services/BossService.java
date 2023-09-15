package com.webdorphin.bot.homeworkchecker.services;

import com.webdorphin.bot.homeworkchecker.dto.telegram.IncomingMessage;

public interface BossService {
    void executeBossCommand(IncomingMessage incomingMessage);
}
