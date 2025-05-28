package com.easyterview.wingterview.user.entity;

import com.easyterview.wingterview.chat.entity.ChatEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.easyterview.wingterview.matching.entity.MatchingParticipantEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
@ToString
public class UserEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "name", nullable = false, length = 50)
    @Builder.Default
    private String name = "temp";

    @Column(name = "nickname", nullable = false, length = 50)
    @Builder.Default
    private String nickname = "temp";

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "curriculum", nullable = false, length = 10)
    @Builder.Default
    private String curriculum = "temp";

    @Column(name = "seat")
    private Integer seat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at", nullable = false)
    private Timestamp modifiedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshTokenEntity refreshToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserJobInterestEntity> userJobInterest = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserTechStackEntity> userTechStack = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewStatEntity interviewStat;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private MatchingParticipantEntity matchingParticipant;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterviewParticipantEntity> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReceivedQuestionEntity> receivedQuestionEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<UserChatroomEntity> userChatroomEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<RecordingEntity> recordingEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatEntity> chatList = new ArrayList<>();
}
