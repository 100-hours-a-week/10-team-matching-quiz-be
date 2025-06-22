package com.easyterview.wingterview.interview.service.question;


import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.user.entity.UserEntity;

public interface QuestionGenerator {
    Object generate(InterviewEntity interview, UserEntity user, QuestionCreationRequestDto dto);
}
