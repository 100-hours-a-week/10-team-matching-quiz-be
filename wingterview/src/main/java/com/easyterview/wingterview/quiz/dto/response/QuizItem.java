package com.easyterview.wingterview.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // ← @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 포함
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItem {
    private String difficulty;
    private String question;
    private List<String> options;
    private Integer answerIndex;
    private String explanation;
}
