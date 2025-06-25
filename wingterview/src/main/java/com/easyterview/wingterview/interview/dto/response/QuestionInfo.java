package com.easyterview.wingterview.interview.dto.response;

import java.util.List;

public record QuestionInfo(int questionIdx, String selectedQuestion, List<String> questionOptions) {
}

