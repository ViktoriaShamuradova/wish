package com.example.wish.component;

import com.example.wish.dto.*;
import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.FinishedWish;
import com.example.wish.entity.Wish;

import java.text.ParseException;


public interface WishMapper {

    Wish convertToEntity(AbstractWishDto wishDto) throws ParseException;

    WishDto convertToDto(Wish wish);

    SearchWishDto convertToSearchDto(Wish wish);

    ExecutingWishDto convertToDto(ExecutingWish executingWish);

    FinishedWishDto convertToDto(FinishedWish w);
}