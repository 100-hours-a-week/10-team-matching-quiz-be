// 새로운 엔티티: InterviewParticipantEntity.java
package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.interview.enums.ParticipantRole;
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
@Table(name = "interview_participant")
public class InterviewParticipantEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private InterviewEntity interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "role", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    public static InterviewParticipantEntity toEntity(UserEntity user, InterviewEntity interview){
        return InterviewParticipantEntity.builder()
                .user(user)
                .role(ParticipantRole.SECOND_INTERVIEWER)
                .interview(interview)
                .build();
    }
}