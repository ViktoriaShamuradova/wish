package com.example.wish.service;

import com.example.wish.dto.*;
import com.example.wish.model.search_request.WishSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;


public interface WishService {

    MainScreenProfileDto create(CreateWishRequest wishDto, MultipartFile file) throws ParseException, IOException;

    MainScreenProfileDto update(long wishId, CreateWishRequest wishDto, MultipartFile file) throws ParseException, IOException;

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
}
