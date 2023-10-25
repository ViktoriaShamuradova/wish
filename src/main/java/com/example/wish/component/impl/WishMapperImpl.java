package com.example.wish.component.impl;

import com.example.wish.component.WishMapper;
import com.example.wish.dto.wish.*;
import com.example.wish.entity.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@RequiredArgsConstructor
@Component
public class WishMapperImpl implements WishMapper {

    private final ModelMapper modelMapper;

    @Override
    public Wish convertToEntity(AbstractWishDto abstDto) {

        Wish wish = modelMapper.map(abstDto, Wish.class);
        if (abstDto instanceof WishDto) {
            WishDto wishDto = (WishDto) abstDto;
            wish.setId(wishDto.getId());
            wish.setWatchCount((wishDto.getWatchCount()));
            wish.setStatus(wishDto.getWishStatus());
        }
        if (abstDto instanceof ExecutingWishDto) {

        }
        return wish;
    }

    @Override
    public WishDto convertToDto(Wish wish) {
        WishDto wishDto = modelMapper.map(wish, WishDto.class);
        wishDto.setTagNames(wish.getTags().stream().map(Tag::getTagName).collect(Collectors.toSet()));
        return wishDto;
    }

    @Override
    public WishImageDto convertToDto(WishImage image) {
        WishImageDto wishImageDto = modelMapper.map(image, WishImageDto.class);
        return wishImageDto;
    }

    @Override
    public Wish convertToEntity(CreateWishRequest createWishRequest) {
        Wish wish = modelMapper.map(createWishRequest, Wish.class);
        return wish;
    }

    @Override
    public SearchWishDto convertToSearchDto(Wish wish) {
        WishDto wishDto = convertToDto(wish);
        SearchWishDto searchWishDto = new SearchWishDto(wishDto);
        searchWishDto.setProfileUid(wish.getOwnProfile().getUid());
        searchWishDto.setProfileFirstname(wish.getOwnProfile().getFirstName());
        searchWishDto.setProfileLastname(wish.getOwnProfile().getLastName());

        return searchWishDto;
    }

    @Override
    public ExecutingWishDto convertToDto(ExecutingWish executingWish) {
        ExecutingWishDto executingWishDto = modelMapper.map(executingWish, ExecutingWishDto.class);
        if (executingWishDto.isAnonymously()) {
            executingWishDto.setExecutingProfileId(null);

        }
        executingWishDto.setWishId(executingWish.getWish().getId());
        //   executingWishDto.setPhoto(executingWish.getWish().getPhoto());
        executingWishDto.setTitle(executingWish.getWish().getTitle());
        executingWishDto.setDescription(executingWish.getWish().getDescription());
        executingWishDto.setPriority(executingWish.getWish().getPriority());
       // executingWishDto.setTags(executingWish.getWish().getTags());
        executingWishDto.setCreated(executingWish.getWish().getCreated());

        return executingWishDto;
    }

    @Override
    public FinishedWishDto convertToDto(FinishedWish w) {
        FinishedWishDto finishedWishDto = modelMapper.map(w, FinishedWishDto.class);
        finishedWishDto.setTitle(w.getWish().getTitle());
        finishedWishDto.setDescription(w.getWish().getDescription());
        finishedWishDto.setPriority(w.getWish().getPriority());
        //finishedWishDto.setTags(w.getWish().getTags());
        finishedWishDto.setCreated(w.getWish().getCreated());
        finishedWishDto.setWishId(w.getWish().getId());

        finishedWishDto.setEarnKarma(w.getEarnKarma());
        // finishedWishDto.setPhoto(w.getWish().getPhoto());

        if (finishedWishDto.isAnonymously()) {
            finishedWishDto.setExecutedProfileId(null);
        } else {
            finishedWishDto.setExecutedProfileId(w.getExecutedProfile().getId());
        }

        return finishedWishDto;
    }


}
