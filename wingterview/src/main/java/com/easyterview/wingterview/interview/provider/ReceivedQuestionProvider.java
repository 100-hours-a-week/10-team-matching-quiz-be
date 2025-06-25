package com.easyterview.wingterview.interview.provider;

import com.easyterview.wingterview.interview.repository.ReceivedQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceivedQuestionProvider {
    private final ReceivedQuestionRepository receivedQuestionRepository;

}
