package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.UserChatroomEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserChatroomRepository extends JpaRepository<UserChatroomEntity, UUID> {

    List<UserChatroomEntity> findAllByInterviewId(UUID uuid);
}
