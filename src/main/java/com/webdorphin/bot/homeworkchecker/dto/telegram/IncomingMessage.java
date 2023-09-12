package com.webdorphin.bot.homeworkchecker.dto.telegram;

import lombok.Data;

@Data
public class IncomingMessage {

    private String user;
    private boolean isAdmin;
    private boolean isVerifiedUser;
    private String message;

}
