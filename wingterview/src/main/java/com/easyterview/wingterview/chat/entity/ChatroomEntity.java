package com.easyterview.wingterview.chat.entity;

import com.easyterview.wingterview.user.entity.UserChatroomEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "chatroom")
public class ChatRoomEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @OneToMany(mappedBy = "chatroom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserChatroomEntity> userChatroomList = new ArrayList<>();

    @OneToMany(mappedBy = "chatroom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatEntity> chatEntityList = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;
}
