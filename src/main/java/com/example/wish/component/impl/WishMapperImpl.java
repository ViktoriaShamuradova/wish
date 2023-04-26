package com.example.wish.component.impl;

import com.example.wish.component.KarmaCounter;
import com.example.wish.component.WishMapper;
import com.example.wish.dto.*;
import com.example.wish.entity.*;
import com.example.wish.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class WishMapperImpl implements WishMapper {

    private final ModelMapper modelMapper;
    private final KarmaCounter karmaCounter;

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
        wishDto.setPhoto(ImageUtil.decompressImage(wish.getPhoto()));

        return wishDto;
    }

    @Override
    public SearchWishDto convertToSearchDto(Wish wish) {
        WishDto wishDto = convertToDto(wish);
        SearchWishDto searchWishDto = new SearchWishDto(wishDto);
        searchWishDto.setProfileUid(wish.getOwnProfile().getUid());
        searchWishDto.setProfileFirstname(wish.getOwnProfile().getFirstname());
        searchWishDto.setProfileLastname(wish.getOwnProfile().getLastname());

        return searchWishDto;
    }

    @Override
    public ExecutingWishDto convertToDto(ExecutingWish executingWish) {
        ExecutingWishDto executingWishDto = modelMapper.map(executingWish, ExecutingWishDto.class);
        if (executingWishDto.getExecutingStatus() == ExecuteStatus.IN_PROGRESS_ANONYMOUS) {
            executingWishDto.setExecutingProfileId(null);
            executingWishDto.setExecutingProfileUid(null);
            executingWishDto.setExecutingProfilePhoto(null);
        }
        executingWishDto.setWishId(executingWish.getWish().getId());
        executingWishDto.setPhoto(ImageUtil.decompressImage(executingWish.getWish().getPhoto()));
        executingWishDto.setTitle(executingWish.getWish().getTitle());
        executingWishDto.setDescription(executingWish.getWish().getDescription());
        executingWishDto.setPriority(executingWish.getWish().getPriority());
        executingWishDto.setTags(executingWish.getWish().getTags());
        executingWishDto.setCreated(executingWish.getWish().getCreated());

        return executingWishDto;
    }

    @Override
    public FinishedWishDto convertToDto(FinishedWish w) {
        FinishedWishDto finishedWishDto = modelMapper.map(w, FinishedWishDto.class);
        finishedWishDto.setTitle(w.getWish().getTitle());
        finishedWishDto.setDescription(w.getWish().getDescription());
        finishedWishDto.setPriority(w.getWish().getPriority());
        finishedWishDto.setTags(w.getWish().getTags());
        finishedWishDto.setCreated(w.getWish().getCreated());
        finishedWishDto.setWishId(w.getWish().getId());

        finishedWishDto.setEarnKarma(w.getEarnKarma());
        finishedWishDto.setPhoto(ImageUtil.decompressImage(w.getWish().getPhoto()));
        finishedWishDto.setOwnProfileUid(w.getWish().getOwnProfile().getUid());
        finishedWishDto.setOwnProfileFullName(w.getWish().getOwnProfile().getLastname() + " " + w.getWish().getOwnProfile().getFirstname());

        if (finishedWishDto.getStatus() == FinishWishStatus.FINISHED_FAILED_ANONYMOUS) {
            finishedWishDto.setExecutedProfileId(null);
            finishedWishDto.setExecutedProfileUid(null);
            finishedWishDto.setExecutedProfilePhoto(null);
        } else {
            finishedWishDto.setExecutedProfileId(w.getExecutedProfile().getId());
            finishedWishDto.setExecutedProfileUid(w.getExecutedProfile().getUid());
            finishedWishDto.setExecutedProfilePhoto(ImageUtil.decompressImage(w.getWish().getOwnProfile().getPhoto()));

        }

        return finishedWishDto;
    }
}
