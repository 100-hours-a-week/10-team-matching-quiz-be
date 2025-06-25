package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.repository.InterviewHistoryRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewHistoryProvider {
    private final InterviewHistoryRepository interviewHistoryRepository;

    public InterviewHistoryEntity getInterviewHistoryOrThrow(UserEntity user){
        return interviewHistoryRepository
                .findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(InterviewNotFoundException::new);
    }
}
