package com.webdorphin.bot.homeworkchecker.services.impl;

import com.webdorphin.bot.homeworkchecker.dto.api.CreateUserRequest;
import com.webdorphin.bot.homeworkchecker.exceptions.GroupNotFoundException;
import com.webdorphin.bot.homeworkchecker.exceptions.UserNotFoundException;
import com.webdorphin.bot.homeworkchecker.model.Student;
import com.webdorphin.bot.homeworkchecker.model.User;
import com.webdorphin.bot.homeworkchecker.repositories.GroupRepository;
import com.webdorphin.bot.homeworkchecker.repositories.UserRepository;
import com.webdorphin.bot.homeworkchecker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public User find(org.telegram.telegrambots.meta.api.objects.User telegramUser) throws UserNotFoundException {
        var username = telegramUser.getUserName();
        return find(username);
    }

    @Override
    public User find(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found by username=" + username));
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) throws GroupNotFoundException {
        var group = groupRepository.findByCode(createUserRequest.getGroupCode())
                .orElseThrow(() -> new GroupNotFoundException("Group with code " + createUserRequest.getGroupCode() + " is not found"));

        var newStudent = new Student();
        newStudent.setName(createUserRequest.getFirstname());
        newStudent.setLastname(createUserRequest.getLastname());
        newStudent.setGroup(group);

        var newUser = new User();
        newUser.setUsername(createUserRequest.getUsername());
        newUser.setStudent(newStudent);

        return userRepository.save(newUser);
    }
}
