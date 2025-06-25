package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.Partner;
import com.easyterview.wingterview.interview.dto.response.QuestionInfo;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.user.entity.UserEntity;

public class InterviewStatusMapper {

    public static InterviewStatusDto of(InterviewEntity interview, Integer timeRemain, Boolean isInterviewer, Partner partner, QuestionInfo questionInfo){
        return InterviewStatusDto.builder()
                .interviewId(String.valueOf(interview.getId()))
                .timeRemain(timeRemain)
                .currentRound(interview.getRound())
                .currentPhase(interview.getPhase().getPhase())
                .isInterviewer(isInterviewer)
                .isAiInterview(false)
                .partner(partner)
                .questionIdx(questionInfo.questionIdx())
                .selectedQuestion(questionInfo.selectedQuestion())
                .questionOption(questionInfo.questionOptions())
                .build();
    }
}
