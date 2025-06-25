package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.common.util.mapper.entity.QuestionHistoryMapper;
import com.easyterview.wingterview.common.util.mapper.entity.QuestionOptionsMapper;
import com.easyterview.wingterview.common.util.mapper.entity.ReceivedQuestionMapper;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;
import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.easyterview.wingterview.interview.provider.InterviewProvider;
import com.easyterview.wingterview.interview.provider.QuestionOptionsProvider;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import com.easyterview.wingterview.interview.repository.QuestionHistoryRepository;
import com.easyterview.wingterview.interview.repository.QuestionOptionsRepository;
import com.easyterview.wingterview.interview.repository.ReceivedQuestionRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.provider.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionSelectionServiceImpl implements QuestionSelectionService {

    private final QuestionHistoryRepository questionHistoryRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final InterviewProvider interviewProvider;
    private final QuestionOptionsProvider questionOptionsProvider;

    @Override
    @Transactional
    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {
        InterviewEntity interview = interviewProvider.getInterviewOrThrow(interviewId);
        List<QuestionOptionsEntity> options = questionOptionsProvider.getLastOptions(interview);
        validateSelectedIndex(dto.getSelectedIdx(), options.size());

        String selectedQuestion = options.get(dto.getSelectedIdx()).getOption();

        saveOrUpdateQuestionHistory(interview, selectedQuestion);
        saveReceivedQuestion(interview, selectedQuestion);
        questionOptionsRepository.deleteAllByInterview(interview);
    }

    // ======== 👇 헬퍼 메서드들 👇 ========

    private void validateSelectedIndex(int selectedIdx, int optionSize) {
        if (selectedIdx < 0 || selectedIdx >= optionSize) {
            throw new IllegalArgumentException("선택한 질문 인덱스가 유효하지 않습니다.");
        }
    }

    private void saveOrUpdateQuestionHistory(InterviewEntity interview, String selectedQuestion) {
        Optional<QuestionHistoryEntity> oldOpt = questionHistoryRepository.findByInterview(interview);
        if (oldOpt.isPresent()) {
            QuestionHistoryEntity old = oldOpt.get();
            old.setSelectedQuestionIdx(old.getSelectedQuestionIdx() + 1);
            old.setSelectedQuestion(selectedQuestion);
            questionHistoryRepository.save(old);
        } else {
            questionHistoryRepository.save(QuestionHistoryMapper.toEntity(interview, selectedQuestion));
        }
    }

    private void saveReceivedQuestion(InterviewEntity interview, String selectedQuestion) {
        UserEntity partner = interview.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(UUIDUtil.getUserIdFromToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("상대방 참가자를 찾을 수 없습니다."))
                .getUser();

        receivedQuestionRepository.save(ReceivedQuestionMapper.toEntity(selectedQuestion,partner));
    }
}
