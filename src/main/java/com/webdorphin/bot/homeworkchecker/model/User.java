package com.webdorphin.bot.homeworkchecker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "chat_id")
    private Long chatId;

    @OneToOne(cascade = CascadeType.ALL)
    private Student student;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Assignment> assignments;

    @Column
    private Boolean isAdmin;
}
