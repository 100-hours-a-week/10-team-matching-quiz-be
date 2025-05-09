package com.easyterview.wingterview.chat.repository;

import com.easyterview.wingterview.chat.entity.ChatroomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatroomRepository extends JpaRepository<ChatroomEntity, UUID> {
}
