package com.example.wish.service;

import com.example.wish.entity.ProfileVisitorWish;
import com.example.wish.entity.Wish;

public interface ProfileVisitorService {

    void save(ProfileVisitorWish profileVisitorWish);

    Integer countProfilesByWish(Wish wish);
}
