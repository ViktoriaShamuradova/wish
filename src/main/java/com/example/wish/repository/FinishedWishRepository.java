package com.example.wish.repository;

import com.example.wish.entity.FinishWishStatus;
import com.example.wish.entity.FinishedWish;
import com.example.wish.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinishedWishRepository extends JpaRepository<FinishedWish, Long>, JpaSpecificationExecutor<FinishedWish> {

    List<FinishedWish> findByWishOwnProfileId(long ownProfile);

    List<FinishedWish> findByStatusAndWishOwnProfileId(FinishWishStatus status, Long ownProfileId);

    List<FinishedWish> findByStatusAndExecutedProfileId(FinishWishStatus status, Long executedProfileId);



}
