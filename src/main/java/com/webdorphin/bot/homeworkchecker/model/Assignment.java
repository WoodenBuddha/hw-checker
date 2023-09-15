package com.webdorphin.bot.homeworkchecker.model;

import com.webdorphin.bot.homeworkchecker.dto.AssignmentStatus;
import jakarta.persistence.*;
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

    @Lob
    @Column
    private String sourceCode;

    @Column
    private AssignmentStatus status;

    @Column
    private Double grade;

    @Column
    private String taskCode;

    @Lob
    @Column
    private String errorMsg;

    @Column
    private String testCaseError;

    @Column
    private String additionalMsg;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}
