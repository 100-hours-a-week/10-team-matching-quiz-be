package com.easyterview.wingterview.quiz.entity;

import com.easyterview.wingterview.quiz.enums.QuizCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cs_quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsQuizEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizCategory category;

    @Column(nullable = false)
    private String question;

    @Column(name = "answer_idx", nullable = false)
    private Integer answerIdx;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String explanation;

    @OneToMany(mappedBy = "csQuiz", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @Builder.Default
    private List<CsQuizSelectionEntity> choices = new ArrayList<>();

    @OneToMany(mappedBy = "csQuiz", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @Builder.Default
    private List<UserCsQuizEntity> userCsQuizEntityList = new ArrayList<>();

}
