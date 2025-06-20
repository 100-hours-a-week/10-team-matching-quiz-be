package com.easyterview.wingterview.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question_history")
public class QuestionHistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "selected_question", length = 200, nullable = false)
    private String selectedQuestion;

    @Column(name = "selected_question_idx", nullable = false)
    @Builder.Default
    private Integer selectedQuestionIdx = 1;

    @Column(name = "created_at", nullable = false)
    @UpdateTimestamp
    private Timestamp createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private InterviewEntity interview;
}
