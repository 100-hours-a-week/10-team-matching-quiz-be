package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;
import com.easyterview.wingterview.interview.repository.QuestionOptionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionOptionsProvider {
    private final QuestionOptionsRepository questionOptionsRepository;

    public List<QuestionOptionsEntity> getLastOptions(InterviewEntity interview){
        return questionOptionsRepository.findTop4ByInterviewOrderByCreatedAtDesc(interview);
    }

    public List<QuestionOptionsEntity> getPassedOptions(InterviewEntity interview){
        return questionOptionsRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
