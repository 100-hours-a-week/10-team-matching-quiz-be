package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;

public interface QuestionService {
    Object makeQuestion(String interviewId, QuestionCreationRequestDto dto);

    void selectQuestion(String interviewId, QuestionSelectionRequestDto dto);
}
