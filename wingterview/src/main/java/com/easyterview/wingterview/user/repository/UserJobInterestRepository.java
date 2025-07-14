package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.UserJobInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJobInterestRepository extends JpaRepository<UserJobInterestEntity, UUID> {
    void deleteAllByUserId(UUID uuid);
}
