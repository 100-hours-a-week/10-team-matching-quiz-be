package com.easyterview.wingterview.interview.service.question;


import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.MainQuestionEntity;
import com.easyterview.wingterview.interview.provider.MainQuestionProvider;
import com.easyterview.wingterview.interview.repository.MainQuestionRepository;
import com.easyterview.wingterview.user.entity.UserEntity;

import java.util.List;

public interface QuestionGenerator {
    Object generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto);

    default List<String> getJobInterests(UserEntity user) {
        return user.getUserJobInterest().stream()
                .map(j -> j.getJobInterest().name())
                .toList();
    }

    default List<String> getTechStacks(UserEntity user) {
        return user.getUserTechStack().stream()
                .map(t -> t.getTechStack().name())
                .toList();
    }

    default List<String> getQuestionFromJobInterestAndTechStack(MainQuestionProvider mainQuestionProvider, List<String> jobInterests, List<String> techStacks) {
        return mainQuestionProvider.getRandomQuestions(jobInterests,techStacks).stream()
                .map(MainQuestionEntity::getContents)
                .toList();
    }
}
