package com.easyterview.wingterview.user.entity;

import com.easyterview.wingterview.chat.entity.ChatRoomEntity;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
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
@Table(
        name = "user_chatroom",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interview_id"})
)
public class UserChatroomEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID id;

    @Column(name = "interview_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID interviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoomEntity chatRoom;


}
