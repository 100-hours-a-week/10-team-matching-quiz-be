package com.easyterview.wingterview.quiz.entity;

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
@Table(name = "quiz")
public class QuizEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME) // 👈 UUIDv7 방식으로 생성
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(length = 100, nullable = false)
    private String question;

    @Column(name = "user_answer", length = 50, nullable = false)
    private String userAnswer;

    @Column(name = "correct_answer", length = 50, nullable = false)
    private String correctAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String commentary;

    @Column(name = "solved_at", nullable = false)
    @CreationTimestamp
    private Timestamp solvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
