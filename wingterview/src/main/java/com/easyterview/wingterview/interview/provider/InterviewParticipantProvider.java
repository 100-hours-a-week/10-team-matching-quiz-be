package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.global.exception.UserNotParticipatedException;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.repository.InterviewParticipantRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewParticipantProvider {
    private final InterviewParticipantRepository interviewParticipantRepository;

    public InterviewParticipantEntity getInterviewParticipantOrThrow(UserEntity user){
        return interviewParticipantRepository.findByUser(user)
                .orElseThrow(UserNotParticipatedException::new);
    }

}
