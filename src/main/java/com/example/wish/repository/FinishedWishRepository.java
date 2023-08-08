package com.example.wish.repository;

import com.example.wish.entity.FinishWishStatus;
import com.example.wish.entity.FinishedWish;
import com.example.wish.entity.WishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinishedWishRepository extends JpaRepository<FinishedWish, Long>, JpaSpecificationExecutor<FinishedWish> {

    List<FinishedWish> findByWishOwnProfileId(long ownProfile);

    Optional<FinishedWish> findByWishId(Long id); //проверить , потому что ту может быть ошибка

    List<FinishedWish> findByStatusAndWishOwnProfileId(FinishWishStatus status, Long ownProfileId);


    List<FinishedWish> findByExecutedProfileIdAndStatusIn( Long executedProfileId, List<FinishWishStatus> statuses);

    //этот метод используется для возвращения успешно законченного желания. Анонимно или нет
    Optional<FinishedWish> findByWishIdAndStatusIn(Long id, List<FinishWishStatus> statuses);
}
