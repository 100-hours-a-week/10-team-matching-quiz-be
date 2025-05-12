package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question_options")
@ToString
public class QuestionOptionsEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "first_option", length = 100, nullable = false)
    private String firstOption;

    @Column(name = "second_option", length = 100, nullable = false)
    private String secondOption;

    @Column(name = "third_option", length = 100, nullable = false)
    private String thirdOption;

    @Column(name = "fourth_option", length = 100, nullable = false)
    private String fourthOption;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private InterviewEntity interview;
}
