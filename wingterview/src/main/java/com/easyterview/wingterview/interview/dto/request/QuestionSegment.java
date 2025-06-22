package com.easyterview.wingterview.interview.dto.request;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSegment {
    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("start_time")
    private Integer startTime;

    @JsonProperty("end_time")
    private Integer endTime;

    private String question;

    public static List<QuestionSegment> fromEntity(InterviewHistoryEntity interviewHistory){
        return
        interviewHistory.getSegments().stream()
                .map(s -> QuestionSegment.builder()
                        .segmentId(s.getId().toString())
                        .startTime(s.getFromTime())
                        .endTime(s.getToTime())
                        .question(s.getSelectedQuestion())
                        .build()).toList();
    }
}

