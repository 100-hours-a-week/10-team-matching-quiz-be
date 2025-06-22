package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.QuestionOptionNotFoundException;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiQuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiMainQuestionGenerator implements QuestionGenerator {
    private final MainQuestionRepository mainQuestionRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;

    @Override
    public AiQuestionCreationResponseDto generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto) {
        // 희망 직무, 테크스택 관련 메인 질문 뽑아오기
        String question = getQuestionFromJobInterestAndTechStack(getJobInterests(user), getTechStacks(user)).getFirst();
        QuestionHistoryEntity questionHistory = interview.getQuestionHistory();

        // question history가 없다면 새로 만들어서 저장
        if (questionHistory == null) {
            questionHistoryRepository.save(QuestionHistoryEntity.toEntity(interview,question));
        }

        else {
            InterviewHistoryEntity interviewHistory = getLastInterviewHistoryOrElseThrow(user);
            Integer segmentOrder = interviewSegmentRepository.countByInterviewHistory(interviewHistory);
            String oldQuestion = questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new).getSelectedQuestion();
            interviewSegmentRepository.save(InterviewSegmentEntity.toEntity(interviewHistory,segmentOrder,interview,oldQuestion));

            Integer questionIdx = questionHistory.getSelectedQuestionIdx();
            questionHistory.setSelectedQuestion(question);
            questionHistory.setSelectedQuestionIdx(questionIdx + 1);
        }

        receivedQuestionRepository.save(ReceivedQuestionEntity.toEntity(question, user));

        return AiQuestionCreationResponseDto.builder()
                .question(question)
                .build();
    }

    private InterviewHistoryEntity getLastInterviewHistoryOrElseThrow(UserEntity user) {
        return interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(InterviewNotFoundException::new);
    }

    private List<String> getQuestionFromJobInterestAndTechStack(List<String> jobInterests, List<String> techStacks) {
        return mainQuestionRepository
                .findRandomMatchingQuestions(jobInterests, techStacks).stream()
                .map(MainQuestionEntity::getContents)
                .toList();
    }

    private List<String> getTechStacks(UserEntity user) {
        return user.getUserTechStack().stream()
                .map(t -> t.getTechStack().name())
                .toList();
    }

    private List<String> getJobInterests(UserEntity user) {
        return user.getUserJobInterest().stream()
                .map(j -> j.getJobInterest().name())
                .toList();
    }
}
