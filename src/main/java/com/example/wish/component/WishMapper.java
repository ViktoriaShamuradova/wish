package com.example.wish.component;

import com.example.wish.dto.wish.*;
import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.FinishedWish;
import com.example.wish.entity.Wish;
import com.example.wish.entity.WishImage;


public interface WishMapper {

    Wish convertToEntity(AbstractWishDto wishDto);

    WishDto convertToDto(Wish wish);

    SearchWishDto convertToSearchDto(Wish wish);

    ExecutingWishDto convertToDto(ExecutingWish executingWish);

    FinishedWishDto convertToDto(FinishedWish w);

    WishImageDto convertToDto(WishImage image);

    Wish convertToEntity(CreateWishRequest createWishRequest);
}