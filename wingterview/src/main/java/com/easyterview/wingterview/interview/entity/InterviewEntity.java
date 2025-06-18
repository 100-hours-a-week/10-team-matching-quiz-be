package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview")
public class InterviewEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "round", nullable = false)
    @Builder.Default
    private Integer round = 1;

    @Column(name = "phase", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Phase phase = Phase.PENDING;

    @Column(name = "keywords", length = 200)
    private String keywords;

    @Column(name = "is_ai_interview", nullable = false)
    @Builder.Default
    private Boolean isAiInterview = false;

    // 변경: 인터뷰 참여자 관계
    @OneToMany(mappedBy = "interview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterviewParticipantEntity> participants = new ArrayList<>();

    @OneToMany(mappedBy = "interview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionOptionsEntity> questionOptions = new ArrayList<>();

    @OneToOne(mappedBy = "interview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewTimeEntity interviewTime;

    @OneToOne(mappedBy = "interview", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private QuestionHistoryEntity questionHistory;

}
