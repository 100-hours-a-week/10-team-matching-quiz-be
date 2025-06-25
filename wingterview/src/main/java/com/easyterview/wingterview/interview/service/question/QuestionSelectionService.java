package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;

public interface QuestionSelectionService {
    void selectQuestion(String interviewId, QuestionSelectionRequestDto dto);
}
