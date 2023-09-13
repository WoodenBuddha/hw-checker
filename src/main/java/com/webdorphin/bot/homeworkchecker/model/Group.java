package com.webdorphin.bot.homeworkchecker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column
    private String code;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Student> students;
}
