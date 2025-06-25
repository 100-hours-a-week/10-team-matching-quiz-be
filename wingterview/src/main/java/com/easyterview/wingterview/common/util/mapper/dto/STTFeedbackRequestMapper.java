package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.request.QuestionSegment;
import com.easyterview.wingterview.interview.dto.request.STTFeedbackRequestDto;
import com.easyterview.wingterview.user.entity.RecordingEntity;

import java.util.List;

public class STTFeedbackRequestMapper {
    public static STTFeedbackRequestDto of(List<QuestionSegment> questionSegments, RecordingEntity recordingEntity){
        return STTFeedbackRequestDto.builder()
                .questionLists(questionSegments)
                .recordingUrl(recordingEntity.getUrl())
                .build();
    }
}
