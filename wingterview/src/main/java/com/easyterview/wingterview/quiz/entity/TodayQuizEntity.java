package com.easyterview.wingterview.quiz.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "today_quiz")
public class TodayQuizEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME) // 👈 UUIDv7 방식으로 생성
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(length = 100, nullable = false)
    private String question;

    @Column(name = "user_selection")
    private Integer userSelection;

    @Column(name = "correct_answer_idx", nullable = false)
    private Integer correctAnswerIdx;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String commentary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "todayQuiz", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<QuizSelectionEntity> quizSelectionEntityList = new ArrayList<>();
}
