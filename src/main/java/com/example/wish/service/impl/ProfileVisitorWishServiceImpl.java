package com.example.wish.service.impl;

import com.example.wish.entity.ProfileVisitorWish;
import com.example.wish.entity.Wish;
import com.example.wish.repository.ProfileVisitorWishRepository;
import com.example.wish.service.ProfileVisitorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileVisitorWishServiceImpl implements ProfileVisitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileVisitorWishServiceImpl.class);

    private final ProfileVisitorWishRepository profileVisitorWishRepository;

    /**
     * пытаемся сохранить, если исключение - знначит такая уникальная связка есть и ничего делать не надо
     *
     * @param profileVisitorWish
     */
    @Override
    public void save(ProfileVisitorWish profileVisitorWish) {
        try {
            profileVisitorWishRepository.save(profileVisitorWish);
        } catch (Exception ex) {
            LOGGER.info("failed to save profileVisitorWish " + ex.getMessage());
        }
    }

    public Integer countProfilesByWish(Wish wish) {
        return profileVisitorWishRepository.countDistinctByWish(wish);
    }
}
