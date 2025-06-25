package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.mapper.dto.AiQuestionCreationResponseMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewSegmentMapper;
import com.easyterview.wingterview.common.util.mapper.entity.QuestionHistoryMapper;
import com.easyterview.wingterview.common.util.mapper.entity.ReceivedQuestionMapper;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.QuestionOptionNotFoundException;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiQuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.provider.InterviewHistoryProvider;
import com.easyterview.wingterview.interview.provider.InterviewSegmentProvider;
import com.easyterview.wingterview.interview.provider.MainQuestionProvider;
import com.easyterview.wingterview.interview.provider.QuestionHistoryProvider;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiMainQuestionGenerator implements QuestionGenerator {
    private final MainQuestionProvider mainQuestionProvider;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final InterviewHistoryProvider interviewHistoryProvider;
    private final InterviewSegmentProvider interviewSegmentProvider;

    @Override
    @Transactional
    public AiQuestionCreationResponseDto generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto) {
        // 희망 직무, 테크스택 관련 메인 질문 뽑아오기
        String question = getQuestionFromJobInterestAndTechStack(mainQuestionProvider, getJobInterests(user), getTechStacks(user)).getFirst();
        QuestionHistoryEntity questionHistory = interview.getQuestionHistory();

        // question history가 없다면 새로 만들어서 저장
        if (questionHistory == null) {
            interview.setQuestionHistory(questionHistoryRepository.save(QuestionHistoryMapper.toEntity(interview,question)));
        }

        else {
            // Interview Segment에 직전 question 및 order 저장
            InterviewHistoryEntity interviewHistory = interviewHistoryProvider.getInterviewHistoryOrThrow(user);
            Integer segmentOrder = interviewSegmentProvider.getCurrentSegmentOrder(interviewHistory);
            interviewSegmentRepository.save(InterviewSegmentMapper.toEntity(interviewHistory,segmentOrder,interview,questionHistory));

            // db에 바뀐것 저장
            Integer questionIdx = questionHistory.getSelectedQuestionIdx();
            questionHistory.setSelectedQuestion(question);
            questionHistory.setSelectedQuestionIdx(questionIdx + 1);
        }

        receivedQuestionRepository.save(ReceivedQuestionMapper.toEntity(question, user));

        return AiQuestionCreationResponseMapper.of(question);
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

}
