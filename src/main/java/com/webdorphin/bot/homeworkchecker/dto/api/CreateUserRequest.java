package com.webdorphin.bot.homeworkchecker.dto.api;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String firstname;
    private String lastname;
    private String groupCode;
}
