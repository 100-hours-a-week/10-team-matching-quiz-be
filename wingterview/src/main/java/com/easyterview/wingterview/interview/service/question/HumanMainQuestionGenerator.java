package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.mapper.entity.QuestionOptionsMapper;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;
import com.easyterview.wingterview.interview.provider.MainQuestionProvider;
import com.easyterview.wingterview.interview.repository.InterviewParticipantRepository;
import com.easyterview.wingterview.interview.repository.QuestionOptionsRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.provider.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HumanMainQuestionGenerator implements QuestionGenerator{
    private final MainQuestionProvider mainQuestionProvider;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final UserProvider userProvider;

    @Override
    @Transactional
    public QuestionCreationResponseDto generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto) {
        UserEntity partner = userProvider.findPartner(interview,user);

        // 파트너 희망 직무, 테크스택 관련 메인 질문 뽑아오기
        List<String> questions = getQuestionFromJobInterestAndTechStack(mainQuestionProvider, getJobInterests(partner), getTechStacks(partner));

        // QuestionOptionsEntity 리스트 생성
        List<QuestionOptionsEntity> optionEntities = makeQuestionOptionEntities(interview, questions);

        // 양방향 관계 동기화
        interview.getQuestionOptions().addAll(optionEntities);

        // 일괄 저장
        questionOptionsRepository.saveAll(optionEntities);

        // question responsebody
        return QuestionCreationResponseDto.builder()
                .questions(questions)
                .build();
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

    private List<QuestionOptionsEntity> makeQuestionOptionEntities(InterviewEntity interview, List<String> questions) {
        return questions.stream()
                .map(q -> QuestionOptionsMapper.toEntity(interview,q))
                .toList();
    }
}
