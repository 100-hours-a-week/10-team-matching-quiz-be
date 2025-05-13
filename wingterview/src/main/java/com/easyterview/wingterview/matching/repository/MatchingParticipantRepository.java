package com.easyterview.wingterview.matching.repository;

import com.easyterview.wingterview.matching.entity.MatchingParticipantEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchingParticipantRepository extends JpaRepository<MatchingParticipantEntity, UUID> {

    Optional<MatchingParticipantEntity> findByUser(UserEntity user);

    @Query("""
                SELECT COUNT(mp)
                FROM MatchingParticipantEntity mp
                WHERE mp.user.id NOT IN (
                    SELECT ip.user.id FROM InterviewParticipantEntity ip
                )
            """)
    Integer countNotMatchedParticipants();
}
