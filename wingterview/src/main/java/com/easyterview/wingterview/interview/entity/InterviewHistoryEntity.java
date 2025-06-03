package com.easyterview.wingterview.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_history")
@ToString
public class InterviewHistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", name = "interview_id")
    private UUID interviewId;

    @Column(name = "selected_question", length = 100, nullable = false)
    private String selectedQuestion;

    @Column(name = "from_time", nullable = false)
    private Integer from;

    @Column(name = "to_time", nullable = false)
    private Integer to;


}
