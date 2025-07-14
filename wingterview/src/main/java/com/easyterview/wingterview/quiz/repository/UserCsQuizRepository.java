package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.UserCsQuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserCsQuizRepository extends JpaRepository<UserCsQuizEntity, UUID> {
    List<UserCsQuizEntity> findAllByUserId(UUID uuid);

    @Query("SELECT u FROM UserCsQuizEntity u " +
            "JOIN FETCH u.csQuiz q " +
            "JOIN FETCH q.choices " +
            "WHERE u.user.id = :userId")
    List<UserCsQuizEntity> findAllWithChoicesByUserId(@Param("userId") UUID userId);

    void deleteAllByUserId(UUID uuid);
}
