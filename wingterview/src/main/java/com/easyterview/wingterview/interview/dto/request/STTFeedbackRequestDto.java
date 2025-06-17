package com.easyterview.wingterview.interview.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class STTFeedbackRequestDto {

    @JsonProperty("recording_url")
    private String recordingUrl;

    @JsonProperty("question_lists")
    private List<QuestionSegment> questionLists;
}


