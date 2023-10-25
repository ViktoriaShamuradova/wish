package com.example.wish.repository;

import com.example.wish.entity.WishImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishImageRepository extends JpaRepository<WishImage, Long> {
}
