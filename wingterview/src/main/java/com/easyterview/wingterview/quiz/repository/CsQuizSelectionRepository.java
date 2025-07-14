package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.CsQuizSelectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CsQuizSelectionRepository extends JpaRepository<CsQuizSelectionEntity, UUID> {
}
