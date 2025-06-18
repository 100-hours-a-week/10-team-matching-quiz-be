package com.easyterview.wingterview.board.repository;

import com.easyterview.wingterview.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardRepository extends JpaRepository<BoardEntity, UUID> {
}
