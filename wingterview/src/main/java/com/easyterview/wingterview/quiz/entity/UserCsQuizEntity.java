package com.easyterview.wingterview.quiz.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "user_cs_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCsQuizEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_answer_idx")
    private Integer userAnswerIdx;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "quiz_idx", nullable = false)
    private Integer quizIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cs_questions_id", nullable = false)
    private CsQuizEntity csQuiz;
}
