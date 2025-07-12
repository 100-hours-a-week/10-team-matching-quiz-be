package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.UserTechStackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserTechStackRepository extends JpaRepository<UUID, UserTechStackEntity> {
    void deleteAllByUserId(UUID uuid);
}
