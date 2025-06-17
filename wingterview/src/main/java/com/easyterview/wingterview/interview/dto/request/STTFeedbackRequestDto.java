package com.easyterview.wingterview.interview.dto.request;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class STTFeedbackRequestDto {
    private String recordingUrl;
    private List<QuestionSegment> questionLists;
}


