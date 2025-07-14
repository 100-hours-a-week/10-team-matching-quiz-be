package com.easyterview.wingterview.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "cs_quiz_selection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsQuizSelectionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, name = "is_answer")
    private Boolean isAnswer;

    @Column(/*nullable = false, */name = "option_idx")
    private Integer optionIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CsQuizEntity csQuiz;
}
