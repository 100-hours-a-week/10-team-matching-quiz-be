package com.easyterview.wingterview.quiz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class QuizCreationRequestDto {
    // user_id로 나중엔 바뀌어야함
    @JsonProperty("interview_id")
    private String userId;

    @JsonProperty("question_history_list")
    private List<String> questionHistoryList;
}
