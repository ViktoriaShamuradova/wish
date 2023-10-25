package com.example.wish.repository;

import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutingWishRepository extends JpaRepository<ExecutingWish, Long>, JpaSpecificationExecutor<ExecutingWish> {

    Optional<ExecutingWish> findByWishId(Long id);
    List<ExecutingWish> findByExecutingProfileId(Long id);

    List<ExecutingWish> findByFinishBefore(Date currentTime);

    void deleteByExecutingProfile(Profile executedProfile);
}