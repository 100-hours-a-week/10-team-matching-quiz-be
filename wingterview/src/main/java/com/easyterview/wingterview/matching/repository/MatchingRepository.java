package com.easyterview.wingterview.matching.repository;

import com.easyterview.wingterview.matching.entity.MatchingEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchingRepository extends JpaRepository<MatchingEntity, UUID> {

    Optional<MatchingEntity> findByUser(UserEntity user);
}
