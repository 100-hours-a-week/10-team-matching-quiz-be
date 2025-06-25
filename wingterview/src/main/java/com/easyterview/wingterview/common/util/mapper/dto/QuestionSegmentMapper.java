package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.request.QuestionSegment;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;

import java.util.List;

public class QuestionSegmentMapper {
    public static List<QuestionSegment> of(InterviewHistoryEntity interviewHistory){
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
