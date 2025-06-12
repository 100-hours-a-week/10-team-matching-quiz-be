package com.easyterview.wingterview.quiz.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
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
@Table(name = "today_quiz_selection")
public class QuizSelectionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME) // 👈 UUIDv7 방식으로 생성
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "selection_idx", nullable = false)
    private Integer selectionIdx;

    @Column(length = 50, nullable = false)
    private String selection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_quiz_id", nullable = false)
    private TodayQuizEntity todayQuiz;
}
