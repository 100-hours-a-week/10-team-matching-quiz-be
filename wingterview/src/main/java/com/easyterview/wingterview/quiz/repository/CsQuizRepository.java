package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.CsQuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CsQuizRepository extends JpaRepository<CsQuizEntity, UUID> {
    @Query(value = "SELECT * FROM cs_quiz WHERE category = :category ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<CsQuizEntity> findTop10RandomByCategory(@Param("category") String category);

}
