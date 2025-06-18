package com.easyterview.wingterview.rabbitmq.consumer;

import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.dto.response.FeedbackResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewFeedbackEntity;
import com.easyterview.wingterview.interview.entity.InterviewSegmentEntity;
import com.easyterview.wingterview.interview.repository.InterviewFeedbackRepository;
import com.easyterview.wingterview.interview.repository.InterviewSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackConsumer {

    private final InterviewSegmentRepository interviewSegmentRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;

    @Transactional
    public void consumeFeedback(FeedbackResponseDto responseDto) {
        log.info("📩 피드백 응답 수신: {}", responseDto);

        responseDto.getFeedbackLists().forEach(feedbackItem -> {
            InterviewSegmentEntity segment = interviewSegmentRepository.findById(UUID.fromString(feedbackItem.getSegmentId())).orElseThrow(InterviewNotFoundException::new);

            int score = feedbackItem.getFeedback().charAt(0) - '0';

            InterviewFeedbackEntity interviewFeedback = InterviewFeedbackEntity.builder()
                    .commentary(feedbackItem.getFeedback().substring(4))
                    .correctAnswer(feedbackItem.getModelAnswer())
                    .interviewSegment(segment)
                    .score(score)
                    .build();

            interviewFeedbackRepository.save(interviewFeedback);

            segment.setFeedback(interviewFeedback);
        });
    }

}
