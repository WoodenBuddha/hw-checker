package com.webdorphin.bot.homeworkchecker.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column
    private String taskCode;

    @Column
    private String input;

    @Column
    private String output;

    @Column
    private Integer variation;

    @Column
    private Double weight;
}
