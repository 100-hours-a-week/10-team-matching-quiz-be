package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
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
@Table(name = "interview_segment")
public class InterviewSegmentEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "selected_question", length = 100, nullable = false)
    private String selectedQuestion;

    @Column(name = "from_time", nullable = false)
    private Integer fromTime;

    @Column(name = "to_time", nullable = false)
    private Integer toTime;

    @ManyToOne
    @JoinColumn(name = "interview_history_id", nullable = false)
    private InterviewHistoryEntity interviewHistory;

    @Column(name = "segment_order", nullable = false) // 세그먼트 순서
    private Integer segmentOrder;

    // 양방향 관계 설정 (optional = true로 설정하여 피드백이 없을 수도 있음을 명시)
    @OneToOne(mappedBy = "interviewSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InterviewFeedbackEntity feedback;
}
