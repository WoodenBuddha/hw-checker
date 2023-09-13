package com.webdorphin.bot.homeworkchecker.model;

import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;


    @Column
    private String sourceCode;

    @Column
    private AssignmentStatus status;

    @Column
    private Double grade;

    @Column
    private String taskCode;

    @Column
    private String errorMsg;

    @Column
    private String testCaseError;
}
