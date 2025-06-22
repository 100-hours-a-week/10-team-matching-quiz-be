package com.easyterview.wingterview.interview.service.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionGenerationFacade {
    private final AiMainQuestionGenerator aiMainQuestionGenerator;
    private final HumanMainQuestionGenerator humanMainQuestionGenerator;
    private final FollowUpQuestionGenerator followUpQuestionGenerator;
}
