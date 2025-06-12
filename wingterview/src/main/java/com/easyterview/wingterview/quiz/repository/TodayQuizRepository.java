package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.TodayQuizEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodayQuizRepository extends JpaRepository<TodayQuizEntity, UUID> {
    List<TodayQuizEntity> findByUser(UserEntity user);

    List<TodayQuizEntity> findByUserId(UUID userId);
}
