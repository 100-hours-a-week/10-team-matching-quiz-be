package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.InvalidUUIDFormatException;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService{

    private final InterviewRepository interviewRepository;

    @Override
    @Transactional
    public NextRoundDto goNextStage(String interviewId) {
        try {
            Optional<InterviewEntity> interviewOpt = interviewRepository.findById(UUID.fromString(interviewId));
            if(interviewOpt.isEmpty()){
                throw new InterviewNotFoundException();
            }

            InterviewEntity interview = interviewOpt.get();
            InterviewStatus nextStatus = InterviewUtil.nextPhase(interview.getRound(), interview.getPhase(), interview.getIsAiInterview());
            interview.setPhase(nextStatus.getPhase());
            interview.setRound(nextStatus.getRound());
            return NextRoundDto.builder()
                    .currentPhase(nextStatus.getPhase().getPhase())
                    .currentRound(nextStatus.getRound())
                    .build();
        }
        catch (IllegalArgumentException e){
            throw new InvalidUUIDFormatException();
        }
    }
}
