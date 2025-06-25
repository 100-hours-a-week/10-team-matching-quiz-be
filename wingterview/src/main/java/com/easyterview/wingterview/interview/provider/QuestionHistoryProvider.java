package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.global.exception.QuestionOptionNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;
import com.easyterview.wingterview.interview.repository.QuestionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuestionHistoryProvider {
    private final QuestionHistoryRepository questionHistoryRepository;

    public QuestionHistoryEntity getQuestionHistoryOrThrow(InterviewEntity interview){
        return questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new);
    }

    public Optional<QuestionHistoryEntity> getQuestionHistoryOpt(InterviewEntity interview){
        return questionHistoryRepository.findByInterview(interview);
    }
}
