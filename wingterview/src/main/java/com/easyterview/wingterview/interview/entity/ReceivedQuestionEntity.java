package com.easyterview.wingterview.interview.entity;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "received_question")
public class ReceivedQuestionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "contents", length = 200, nullable = false)
    private String contents;

    @Column(name = "received_at", nullable = false)
    private Timestamp receivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public static ReceivedQuestionEntity toEntity(String question, UserEntity user){
        return
                ReceivedQuestionEntity.builder()
                        .contents(question)
                        .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .build();
    }
}
