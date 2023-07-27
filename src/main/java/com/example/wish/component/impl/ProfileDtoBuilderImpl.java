package com.example.wish.component.impl;

import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.component.ProfileMapper;
import com.example.wish.component.WishMapper;
import com.example.wish.dto.*;
import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.dto.wish.ExecutingWishDto;
import com.example.wish.dto.wish.FinishedWishDto;
import com.example.wish.entity.*;
import com.example.wish.repository.ExecutingWishRepository;
import com.example.wish.repository.FinishedWishRepository;
import com.example.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProfileDtoBuilderImpl implements ProfileDtoBuilder {

    private final ProfileMapper profileMapper;
    private final WishRepository wishRepository;
    private final ExecutingWishRepository executingWishRepository;
    private final WishMapper wishMapper;
    private final FinishedWishRepository finishedWishRepository;


    @Override
    public MainScreenProfileDto buildMainScreen(Profile profile) {
        MainScreenProfileDto mainScreenProfileDto = profileMapper.convertToDtoMainScreen(profile);

        List<AbstractWishDto> ownWishesDto = findOwmWishesDto(profile.getId());
        mainScreenProfileDto.setOwnWishes(ownWishesDto);

        List<ExecutingWishDto> executingWishesDto = findExecutingWishesDto(profile.getId());
        mainScreenProfileDto.setOtherWishesInProgress(executingWishesDto);

        return mainScreenProfileDto;
    }

    /**
     * возвращаем AnotherProfileDto только с желаниями, у которых статус new
     *
     * @param profile
     * @return
     */
    @Override
    public ProfilesDetails buildProfileDetails(Profile profile) {
        ProfilesDetails profileDetails = profileMapper.convertToDtoAnother(profile);
        return profileDetails;
    }

    @Override
    public ProfileDto buildProfileDto(Profile profile) {
        ProfileDto profileDto = profileMapper.convertToDto(profile);

        return profileDto;
    }

    private List<AbstractWishDto> findOwnNewWishes(Long profileId) {
        List<AbstractWishDto> wishDtos = new ArrayList<>();
        List<Wish> wishes = wishRepository.findByOwnProfileIdAndStatus(profileId, WishStatus.NEW);
        for (Wish w : wishes) {
            wishDtos.add(wishMapper.convertToDto(w));
        }
        return wishDtos;
    }


    @Override
    public List<AbstractWishDto> findOwmWishesDto(Long profileId) {
        //находим свои желания
        List<Wish> ownWishes = wishRepository.findByOwnProfileId(profileId);
        //некоторые свои желания могут уже кем-то исполняться. нужно их найти и сделать executedto
        return convertIfWishInProgress(ownWishes);
    }


    private List<AbstractWishDto> convertIfWishInProgress(List<Wish> ownWishes) {
        List<AbstractWishDto> ownWishesDto = new ArrayList<>();
        for (Wish w : ownWishes) {
            if (w.getStatus() == WishStatus.NEW) {
                ownWishesDto.add(wishMapper.convertToDto(w));
            } else if (w.getStatus() == WishStatus.IN_PROGRESS) {
                ExecutingWish executingWish = executingWishRepository.findByWishId(w.getId()).get();
                ownWishesDto.add(wishMapper.convertToDto(executingWish));
            }
        }
        return ownWishesDto;
    }

    private Page<AbstractWishDto> convertIfWishInProgress(Page<Wish> ownWishes) {
        List<AbstractWishDto> ownWishesDto = ownWishes.get()
                .map(w -> {
                    if (w.getStatus() == WishStatus.NEW) {
                        return wishMapper.convertToDto(w);
                    } else if (w.getStatus() == WishStatus.IN_PROGRESS) {
                        ExecutingWish executingWish = executingWishRepository.findByWishId(w.getId()).get();
                        return wishMapper.convertToDto(executingWish);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(ownWishesDto, ownWishes.getPageable(), ownWishes.getTotalElements());
    }


    private List<ExecutingWishDto> findExecutingWishesDto(Long profileId) {
        //ищем желания, которые ты исполняешь
        List<ExecutingWish> executingWishes = executingWishRepository.findByExecutingProfileId(profileId);
        List<ExecutingWishDto> executingWishesDto = new ArrayList<>();
        for (ExecutingWish w : executingWishes) {
            executingWishesDto.add(wishMapper.convertToDto(w));
        }
        return executingWishesDto;
    }

    private List<FinishedWishDto> findOwnFinishedWishesDto(Profile profile) {
        //ищем твои завершенные желания
        List<FinishedWish> finishedWishes = finishedWishRepository.findByWishOwnProfileId(profile.getId());
        List<FinishedWishDto> finishedWishDtos = new ArrayList<>();
        for (FinishedWish w : finishedWishes) {
            finishedWishDtos.add(wishMapper.convertToDto(w));
        }
        return finishedWishDtos;
    }


}
