package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewSegmentEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class InterviewSegmentMapper {
    public static InterviewSegmentEntity toEntity(InterviewHistoryEntity interviewHistory, Integer currentSegmentOrder, InterviewEntity interview, QuestionHistoryEntity history) {
        return
                InterviewSegmentEntity.builder()
                        .interviewHistory(interviewHistory)
                        .segmentOrder(currentSegmentOrder + 1)
                        .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), history.getCreatedAt()))
                        .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), Timestamp.valueOf(LocalDateTime.now())))
                        .selectedQuestion(history.getSelectedQuestion())
                        .build();
    }
}
