package com.example.wish.repository;

import com.example.wish.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByTokenAndCreatedAtIsNotNull(String token);

    Optional<ConfirmationToken> findByTokenAndCreatedAtIsNull(String token);

    Optional<ConfirmationToken> findByToken(String token);

    void deleteByCreatedAtIsNotNull();

    int countByToken(String token);

    @Modifying
    @Query("UPDATE ConfirmationToken c " +
            "SET c.confirmedAt = ?2 " +
            "WHERE c.token = ?1")
    int updateConfirmedAt(String token,
                          LocalDateTime confirmedAt);


}
