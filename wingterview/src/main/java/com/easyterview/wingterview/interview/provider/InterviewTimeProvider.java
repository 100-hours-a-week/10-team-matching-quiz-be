package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewTimeEntity;
import com.easyterview.wingterview.interview.repository.InterviewTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewTimeProvider {
    private final InterviewTimeRepository interviewTimeRepository;

    public InterviewTimeEntity getInterviewTimeOrThrow(InterviewEntity interview){
        return interviewTimeRepository
                .findByInterview(interview)
                .orElseThrow(InterviewNotFoundException::new);
    }
}
