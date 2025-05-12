package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    @Query("SELECT u.seat FROM UserEntity u WHERE u.seat IS NOT NULL")
    List<Integer> findAllSeatInfo();

    Optional<UserEntity> findBySeat(Integer seatIdx);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.seat = :seat")
    Optional<UserEntity> findBySeatForUpdate(@Param("seat") int seat);

}
