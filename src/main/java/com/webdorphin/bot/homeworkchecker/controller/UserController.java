package com.webdorphin.bot.homeworkchecker.controller;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateUserRequest;
import com.webdorphin.bot.homeworkchecker.exceptions.GroupNotFoundException;
import com.webdorphin.bot.homeworkchecker.exceptions.MalformedRequest;
import com.webdorphin.bot.homeworkchecker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        try {

            Optional.ofNullable(request)
                    .filter(r -> r.getUsername() != null)
                    .filter(r -> r.getLastname() != null)
                    .filter(r -> r.getFirstname() != null)
                    .filter(r -> r.getGroupCode() != null)
                    .orElseThrow(() -> new MalformedRequest("Insufficient data"));

            userService.createUser(request);
        } catch (MalformedRequest malformedRequest) {
            return ResponseEntity.badRequest().body(malformedRequest.getMessage());
        } catch (GroupNotFoundException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
