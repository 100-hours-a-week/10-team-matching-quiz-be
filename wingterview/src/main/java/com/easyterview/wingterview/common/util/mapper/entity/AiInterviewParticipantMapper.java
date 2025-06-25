package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.user.entity.UserEntity;

public class AiInterviewParticipantMapper {
    public static InterviewParticipantEntity toEntity(UserEntity user, InterviewEntity interview){
        return InterviewParticipantEntity.builder()
                .user(user)
                .role(ParticipantRole.SECOND_INTERVIEWER)
                .interview(interview)
                .build();
    }
}
