package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.TodayQuizEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TodayQuizRepository extends JpaRepository<TodayQuizEntity, UUID> {
    List<TodayQuizEntity> findByUser(UserEntity user);

    List<TodayQuizEntity> findByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TodayQuizEntity tq WHERE tq.user = :user")
    void deleteAllByUser(@Param("user") UserEntity user);
}
