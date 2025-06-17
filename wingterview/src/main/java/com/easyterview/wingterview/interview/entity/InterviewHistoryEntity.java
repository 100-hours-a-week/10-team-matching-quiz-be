package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_history")
public class InterviewHistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 양방향 관계 설정
    @OneToMany(mappedBy = "interviewHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterviewSegmentEntity> segments;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(name = "end_at")
    private Timestamp endAt;

    @Column(name = "is_feedback_requested", nullable = false)
    @Builder.Default
    private Boolean isFeedbackRequested = false;
}
