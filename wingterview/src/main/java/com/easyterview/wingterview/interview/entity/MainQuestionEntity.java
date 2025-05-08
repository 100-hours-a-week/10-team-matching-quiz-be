package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.user.enums.JobInterest;
import com.easyterview.wingterview.user.enums.TechStack;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "main_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainQuestionEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(length = 300, nullable = false)
    private String contents;

    @Column(nullable = false)
    private String curriculum;

    // 직무 (단일 Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "job_interest", nullable = false)
    private JobInterest jobInterest;

    // 기술 스택 (Enum 리스트 → 별도 테이블로 관리)
    @ElementCollection(targetClass = TechStack.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "main_question_tech_stack",
            joinColumns = @JoinColumn(name = "main_question_id")
    )
    @Column(name = "tech_stack")
    private List<TechStack> techStacks;
}

