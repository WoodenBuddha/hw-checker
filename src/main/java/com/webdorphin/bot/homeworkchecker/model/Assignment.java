package com.webdorphin.bot.homeworkchecker.model;

import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private LocalDateTime createDate;

    @Column(length = 65534)
    private String sourceCode;

    @Column
    private AssignmentStatus status;

    @Column
    private Double grade;

    @Size(min = 1)
    @Column
    private String taskCode;

    @Column(length = 65534)
    private String errorMsg;

    @Column
    private String testCaseError;

    @Column
    private String additionalMsg;

    @Column(length = 65534)
    private String actualOutput;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}
