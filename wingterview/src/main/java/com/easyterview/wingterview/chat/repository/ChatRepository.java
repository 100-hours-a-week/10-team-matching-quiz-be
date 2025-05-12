package com.easyterview.wingterview.chat.repository;

import com.easyterview.wingterview.chat.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
}
