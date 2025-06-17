package com.easyterview.wingterview.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class FeedbackItem {
    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("model_answer")
    private String modelAnswer;

    private String feedback;
}
