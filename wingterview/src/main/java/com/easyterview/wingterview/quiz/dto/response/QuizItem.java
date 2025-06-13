package com.easyterview.wingterview.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class QuizItem {

    private String difficulty;
    private String question;
    private List<String> options;
    private Integer answerIndex;
    private String explanation;

    @JsonCreator
    public QuizItem(
            @JsonProperty("difficulty") String difficulty,
            @JsonProperty("question") String question,
            @JsonProperty("options") List<String> options,
            @JsonProperty("answerIndex") Integer answerIndex,
            @JsonProperty("explanation") String explanation
    ) {
        this.difficulty = difficulty;
        this.question = question;
        this.options = options;
        this.answerIndex = answerIndex;
        this.explanation = explanation;
    }
}
