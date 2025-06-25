package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.mapper.dto.AiQuestionCreationResponseMapper;
import com.easyterview.wingterview.common.util.mapper.dto.FollowUpQuestionRequestMapper;
import com.easyterview.wingterview.common.util.mapper.dto.QuestionCreationResponseMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewSegmentMapper;
import com.easyterview.wingterview.common.util.mapper.entity.QuestionHistoryMapper;
import com.easyterview.wingterview.common.util.mapper.entity.QuestionOptionsMapper;
import com.easyterview.wingterview.common.util.mapper.entity.ReceivedQuestionMapper;
import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiQuestionCreationResponseDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.provider.InterviewHistoryProvider;
import com.easyterview.wingterview.interview.provider.InterviewSegmentProvider;
import com.easyterview.wingterview.interview.provider.QuestionOptionsProvider;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpQuestionGenerator implements QuestionGenerator {

    private final QuestionOptionsRepository questionOptionsRepository;
    private final RabbitMqService rabbitMqService;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final InterviewHistoryProvider interviewHistoryProvider;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final QuestionOptionsProvider questionOptionsProvider;
    private final InterviewSegmentProvider interviewSegmentProvider;

//      TODO
//    3. history.setSelectedQuestionIdx(history.getSelectedQuestionIdx() + 1)
//      인덱스를 +1 하는 방식이 외부 상태 의존적임 → 사이드이펙트	→ history.increaseQuestionIdx() 같은 메서드로 캡슐화하면 좋음
//    4. Object 반환 타입 (generate)
//      Object 반환은 타입 안정성 부족. 분기라도 인터페이스 상위 타입으로 추상화 추천	→ QuestionCreationResponse 인터페이스 도입 고려


    @Override
    public Object generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto) {
        boolean isRemakeQuestion = isRemakeQuestion(interview, dto);

        FollowUpQuestionRequest request = FollowUpQuestionRequestMapper.of(interview, dto, isRemakeQuestion, getPassedQuestions(interview));

        // 꼬리질문 처리 중

        List<String> response = rabbitMqService.sendFollowUpBlocking(request).getFollowupQuestions();

        if (interview.getIsAiInterview()) {
            saveAiHistory(user, interview, response);
            receivedQuestionRepository.save(ReceivedQuestionMapper.toEntity(response.getFirst(), user));
        } else {
            saveOptions(interview, response);
            // ai 인터뷰가 아니면 received question 저장을 selection에서 하게 됨.
        }

        return createResponse(interview, response);
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

    private boolean isRemakeQuestion(InterviewEntity interview, QuestionCreationRequestDto dto) {
        return interview.getQuestionHistory() != null && dto.getQuestion().equals(interview.getQuestionHistory().getSelectedQuestion());
    }

    private List<String> getPassedQuestions(InterviewEntity interview) {
        return questionOptionsProvider.getPassedOptions(interview)
                .stream()
                .map(QuestionOptionsEntity::getOption)
                .toList();
    }

    // QuestionOption 저장
    private void saveOptions(InterviewEntity interview, List<String> questions) {
        List<QuestionOptionsEntity> questionOptions = questions.stream().map(q -> QuestionOptionsMapper.toEntity(interview, q)).toList();
        interview.getQuestionOptions().addAll(questionOptions);
        questionOptionsRepository.saveAll(questionOptions);
    }

    // QuestionHistory 저장
    private void saveAiHistory(UserEntity user, InterviewEntity interview, List<String> questions) {
        QuestionHistoryEntity history = interview.getQuestionHistory();
        String question = questions.getFirst();
        if (history == null) {
            questionHistoryRepository.save(QuestionHistoryMapper.toEntity(interview, question));
        } else {
            InterviewHistoryEntity interviewHistory = interviewHistoryProvider.getInterviewHistoryOrThrow(user);

            InterviewSegmentEntity interviewSegment = InterviewSegmentMapper.toEntity(interviewHistory,
                    interviewSegmentProvider.getCurrentSegmentOrder(interviewHistory),
                    interview,
                    history);

            interviewSegmentRepository.save(interviewSegment);

            history.setSelectedQuestion(question);
            history.setSelectedQuestionIdx(history.getSelectedQuestionIdx() + 1);
            questionHistoryRepository.save(history);
        }
    }

    // return
    private Object createResponse(InterviewEntity interview, List<String> questions) {
        return interview.getIsAiInterview() ?
                AiQuestionCreationResponseMapper.of(questions.getFirst())
                :
                QuestionCreationResponseMapper.of(questions);
    }
}
