package com.example.wish.repository;

import com.example.wish.entity.ProfileVisitorWish;
import com.example.wish.entity.Wish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileVisitorWishRepository extends JpaRepository<ProfileVisitorWish, Long> {

    Integer countDistinctByWish(Wish wish);
}
