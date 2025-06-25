package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.interview.entity.MainQuestionEntity;
import com.easyterview.wingterview.interview.repository.MainQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class MainQuestionProvider {
    private final MainQuestionRepository mainQuestionRepository;

    public List<MainQuestionEntity> getRandomQuestions(List<String> jobInterests, List<String> techStacks){
        return mainQuestionRepository
                .findRandomMatchingQuestions(jobInterests, techStacks);
    }
}
