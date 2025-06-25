package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.repository.InterviewHistoryRepository;
import com.easyterview.wingterview.interview.repository.InterviewSegmentRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewSegmentProvider {
    private final InterviewSegmentRepository interviewSegmentRepository;

    public Integer getCurrentSegmentOrder(InterviewHistoryEntity interviewHistory){
        return interviewSegmentRepository.countByInterviewHistory(interviewHistory);
    }
}
