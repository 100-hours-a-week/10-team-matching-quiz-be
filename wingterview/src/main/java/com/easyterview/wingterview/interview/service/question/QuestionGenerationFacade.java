package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionGenerationFacade {
    private final AiMainQuestionGenerator aiMainQuestionGenerator;
    private final HumanMainQuestionGenerator humanMainQuestionGenerator;
    private final FollowUpQuestionGenerator followUpQuestionGenerator;
    private final QuestionSelectionService questionSelectionService;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;

    public Object makeQuestion(String interviewId, QuestionCreationRequestDto dto) {
        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                .orElseThrow(InterviewNotFoundException::new);
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(UserNotFoundException::new);

        // 꼬리 질문: dto.question != null
        if (dto.getQuestion() != null) {
            return followUpQuestionGenerator.generate(interview, user, dto);
        }

        // 메인 질문: dto.question == null
        if (interview.getIsAiInterview()) {
            return aiMainQuestionGenerator.generate(interview, user, dto);
        } else {
            return humanMainQuestionGenerator.generate(interview, user, dto);
        }
    }

    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {
        questionSelectionService.selectQuestion(interviewId, dto);
    }
}