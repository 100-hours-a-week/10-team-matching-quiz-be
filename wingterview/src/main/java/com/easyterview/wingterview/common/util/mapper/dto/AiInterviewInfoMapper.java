package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.response.AiInterviewInfoDto;
import com.easyterview.wingterview.interview.dto.response.QuestionInfo;
import com.easyterview.wingterview.interview.entity.InterviewEntity;

public class AiInterviewInfoMapper {
    public static AiInterviewInfoDto of(InterviewEntity interview, Integer timeRemain, QuestionInfo questionInfo){
        return AiInterviewInfoDto.builder()
                .InterviewId(String.valueOf(interview.getId()))
                .currentPhase(interview.getPhase().getPhase())
                .isAiInterview(true)
                .question(questionInfo.selectedQuestion())
                .questionIdx(questionInfo.questionIdx())
                .timeRemain(timeRemain)
                .build();
    }
}
