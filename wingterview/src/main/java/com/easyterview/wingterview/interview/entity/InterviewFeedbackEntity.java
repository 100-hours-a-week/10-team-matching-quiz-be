package com.easyterview.wingterview.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_feedback")
public class InterviewFeedbackEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "correct_answer", columnDefinition = "TEXT") // 모범 답안
    private String correctAnswer;

    @Column(name = "commentary", columnDefinition = "TEXT") // AI 코멘터리
    private String commentary;

    @Column(nullable = false)
    private Integer score;

    @OneToOne
    @JoinColumn(name = "interview_segment_id", nullable = false)
    private InterviewSegmentEntity interviewSegment;
}
