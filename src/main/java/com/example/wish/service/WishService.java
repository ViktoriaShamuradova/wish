package com.example.wish.service;

import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.SearchScreenWishDto;
import com.example.wish.dto.wish.*;
import com.example.wish.entity.Profile;
import com.example.wish.entity.WishStatus;
import com.example.wish.model.search_request.WishSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface WishService {

    WishDto create(CreateWishRequest wishDto);

    void finish(long id);

    AbstractWishDto find(long id);

    void confirm(ConfirmWishRequest id);

    void delete(long id);

    void deleteExecutingWishes(Profile executedProfile);

    void cancelExecution(long id);

    Page<SearchWishDto> find(Pageable pageable, WishSearchRequest request);


    SearchScreenWishDto getSearchScreenWish(Pageable pageable);

    @Transactional
    void execute(long wishId, Boolean anonymously);

    StoryWishDto getStoryWish();

    List<AbstractWishDto> getOwmWishesMainScreen();

    List<AbstractWishDto> getOwnWish(WishStatus wishStatus);

    List<AbstractWishDto> getExecutingWishesMainScreen();

    void uploadImage(Long wishId, MultipartFile file) throws IOException, HttpMediaTypeNotSupportedException;

    byte[] findImage(long wishId);

    WishDto updateByFields(long id, Map<String, Object> fields);

    WishDto update(long id, UpdateWishRequest updateWishRequest);

}
