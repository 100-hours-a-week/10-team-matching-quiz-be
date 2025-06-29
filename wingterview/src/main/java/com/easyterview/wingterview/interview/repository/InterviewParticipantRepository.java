package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipantEntity, UUID> {
    Optional<InterviewParticipantEntity> findByUser(UserEntity user);

    List<InterviewParticipantEntity> findByInterview(InterviewEntity interview);

    Optional<InterviewParticipantEntity> findByUserId(UUID uuid);
}
