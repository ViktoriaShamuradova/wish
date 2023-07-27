package com.example.wish.service;

import com.example.wish.dto.*;
import com.example.wish.dto.wish.ConfirmWishRequest;
import com.example.wish.dto.wish.CreateWishRequest;
import com.example.wish.dto.wish.SearchWishDto;
import com.example.wish.dto.wish.StoryWishDto;
import com.example.wish.model.search_request.WishSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;


public interface WishService {

    MainScreenProfileDto create(CreateWishRequest wishDto) throws ParseException;

    MainScreenProfileDto update(long wishId, CreateWishRequest wishDto) throws ParseException;

    MainScreenProfileDto finish(long id);

    AbstractWishDto find(long id);

    MainScreenProfileDto confirm(ConfirmWishRequest id);

    MainScreenProfileDto delete(long id);

    MainScreenProfileDto cancelExecution(long id);

    Page<SearchWishDto> find(Pageable pageable, WishSearchRequest request);


    SearchScreenWishDto getSearchScreenWish(Pageable pageable);

    @Transactional
    MainScreenProfileDto execute(long wishId, Boolean anonymously);

    StoryWishDto getStoryWish();

    List<AbstractWishDto> getOwmWishesInProgress();

   AbstractWishDto getOwmWish(long wishId);


}
