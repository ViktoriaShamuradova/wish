package com.example.wish.service.impl;

import com.example.wish.component.KarmaCounter;
import com.example.wish.component.NotificationManagerService;
import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.component.WishMapper;
import com.example.wish.dto.*;
import com.example.wish.dto.wish.*;
import com.example.wish.entity.*;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.wish.*;
import com.example.wish.model.CurrentProfile;
import com.example.wish.model.search_request.WishSearchRequest;
import com.example.wish.repository.ExecutingWishRepository;
import com.example.wish.repository.FinishedWishRepository;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.repository.WishRepository;
import com.example.wish.service.ProfileVisitorService;
import com.example.wish.service.WishService;
import com.example.wish.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class WishServiceImpl implements WishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WishServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ExecutingWishRepository executingWishRepository;
    private final FinishedWishRepository finishedWishRepository;
    private final WishRepository wishRepository;
    private final WishMapper wishMapper;
    private final ProfileDtoBuilder profileDtoBuilder;
    private final NotificationManagerService notificationManagerService;
    private final AuthenticationService authenticationService;
    private final KarmaCounter karmaCounter;
    private final ProfileVisitorService profileVisitorService;


    private static final int MAX_COUNT_WISHES = 7;
    private static final long MAX_COUNT_OF_DAYS_TO_FINISH_WISH = 7;
    private static final long MAX_COUNT_OF_DAYS_TO_CONFIRM_WISH = 7;

    @Override
    public MainScreenProfileDto create(CreateWishRequest wishDto) throws ParseException {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishMapper.convertToEntity(wishDto);

        validateNewWish(profile, wish);

        wish.setPhoto(wishDto.getPhoto());
        wish.setStatus(WishStatus.NEW);
        wish.setCreated(Timestamp.from(Instant.now()));

        profile.addInOwnWishes(wish);

        return profileDtoBuilder.buildMainScreen(profileRepository.save(profile));
    }

    @Override
    public MainScreenProfileDto update(long id, CreateWishRequest wishDto) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));
        if (wish.getOwnProfile().getId() != profile.getId())
            throw new WishException("Profile cannot update this wish because it does not belong to their ownWishes list.");

        if (wish.getStatus() == WishStatus.IN_PROGRESS || wish.getStatus() == WishStatus.FINISHED)
            throw new WishException("can't update the wish until it's in progress");

        validateByPriority(profile, wish);

        wish.setTitle(wishDto.getTitle());
        wish.setDescription(wishDto.getDescription());
        wish.setTags(wishDto.getTags());
        wish.setPriority(wishDto.getPriority());

        wish.setPhoto(wishDto.getPhoto());

        wishRepository.save(wish);

        return profileDtoBuilder.buildMainScreen(profileRepository.findById(profile.getId()).get());
    }

    @Override
    public MainScreenProfileDto finish(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));
        ExecutingWish executingWish = executingWishRepository.findByWishId(id).orElseThrow(() -> new WishException("try to finish not executing wish"));
        if (executingWish.getExecutingProfile().getId() != profile.getId())
            throw new WishException("try to finish another's wish");

        if (executingWish.getExecutingStatus() == ExecuteStatus.IN_PROGRESS) {
            executingWish.setExecutingStatus(ExecuteStatus.WAITING_FOR_CONFIRMATION);
        } else {
            executingWish.setExecutingStatus(ExecuteStatus.WAITING_FOR_CONFIRMATION_ANONYMOUS);
        }

        Date finish = executingWish.getFinish();
        finish = Timestamp.from(finish.toInstant().plus(MAX_COUNT_OF_DAYS_TO_CONFIRM_WISH, ChronoUnit.DAYS));
        executingWish.setFinish(finish);
        executingWishRepository.save(executingWish);

        notificationManagerService.sendExecuteWishToOwnerToConfirm(executingWish.getWish().getOwnProfile(), executingWish);

        return profileDtoBuilder.buildMainScreen(profileRepository.findById(profile.getId()).get());
    }

    /**
     * @param confirmWishRequest
     * @return
     */
    @Override
    public MainScreenProfileDto confirm(ConfirmWishRequest confirmWishRequest) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        ExecutingWish executingWish = executingWishRepository.findByWishId(confirmWishRequest.getWishId())
                .orElseThrow(() -> new WishNotFoundException(confirmWishRequest.getWishId()));
        if (executingWish.getWish().getOwnProfile().getId() != currentProfile.getId())
            throw new WishException("can't confirm own wish");

        if (confirmWishRequest.getIsConfirm().equals(true)) {
            return confirmProcess(executingWish, currentProfile.getId());
        } else {
            FinishedWish finishedWish = new FinishedWish(executingWish.getExecutingProfile(),
                    executingWish.getWish(),
                    Timestamp.from(Instant.now()),
                    FinishWishStatus.FINISHED_FAILED,
                    confirmWishRequest.getReasonOfFailedFromOwner());

            finishedWishRepository.save(finishedWish);
            executingWishRepository.delete(executingWish);

            executingWish.getWish().setStatus(WishStatus.NEW);
            wishRepository.save(executingWish.getWish());

            notificationManagerService.sendConfirmWishFailedToExecutor(executingWish.getExecutingProfile());
            notificationManagerService.sendConfirmWishFailedToOwner(executingWish.getWish().getOwnProfile());

            return profileDtoBuilder.buildMainScreen(profileRepository.findById(currentProfile.getId()).get());
        }

    }

    private MainScreenProfileDto confirmProcess(ExecutingWish executingWish, long profileId) {
        FinishedWish finishedWish = new FinishedWish(executingWish.getExecutingProfile(),
                executingWish.getWish(),
                Timestamp.from(Instant.now()),
                FinishWishStatus.FINISHED_SUCCESS);


        Profile executedProfile = karmaCounter.count(executingWish);
        profileRepository.save(karmaCounter.changeStatus(executedProfile));

        finishedWish.setEarnKarma(karmaCounter.calculateAdditionalSumToKarma(executingWish));
        finishedWishRepository.save(finishedWish);
        executingWishRepository.delete(executingWish);

        executingWish.getWish().setStatus(WishStatus.FINISHED);
        wishRepository.save(executingWish.getWish());

        notificationManagerService.sendConfirmWishSuccessToExecutor(executingWish.getExecutingProfile());
        notificationManagerService.sendConfirmWishSuccessToOwner(executingWish.getWish().getOwnProfile());

        return profileDtoBuilder.buildMainScreen(profileRepository.findById(profileId).get());
    }

    @Override
    public MainScreenProfileDto delete(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        if (wish.getOwnProfile().getId() != currentProfile.getId())
            throw new WishException("can't delete not own wish");
        if (wish.getStatus() == WishStatus.IN_PROGRESS) throw new WishException("can't delete in progress wish");

//значит в статусе new или deleted
        wish.setStatus(WishStatus.DELETED);
        wishRepository.save(wish);
        return profileDtoBuilder.buildMainScreen(profileRepository.findById(currentProfile.getId()).get());
    }

    @Override
    public MainScreenProfileDto cancelExecution(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        ExecutingWish executingWish = executingWishRepository.findByWishId(id)
                .orElseThrow(() -> new WishException("you do not execute this wish"));

        if (executingWish.getExecutingProfile().getId() != currentProfile.getId())
            throw new WishException("this wish not your executing wish");

        FinishedWish finishedWish = new FinishedWish();
        finishedWish.setFinish(Timestamp.from(Instant.now()));
        if (executingWish.getExecutingStatus() == ExecuteStatus.IN_PROGRESS_ANONYMOUS) {
            finishedWish.setStatus(FinishWishStatus.FINISHED_FAILED_ANONYMOUS);
        } else {
            finishedWish.setStatus(FinishWishStatus.FINISHED_FAILED);
        }
        finishedWish.setExecutedProfile(executingWish.getExecutingProfile());
        finishedWish.setWish(executingWish.getWish());

        wish.setStatus(WishStatus.NEW);
        wishRepository.save(wish);
        executingWishRepository.delete(executingWish);
        finishedWishRepository.save(finishedWish);

        notificationManagerService.sendCancelExecutionToExecutor(executingWish.getExecutingProfile());
        notificationManagerService.sendCancelExecutionToOwner(executingWish.getWish().getOwnProfile());

        return profileDtoBuilder.buildMainScreen(profileRepository.findById(currentProfile.getId()).get());
    }

    /**
     * get own wishes in progress or new
     * проверить, чтобы не возвращали желания, которые уже завершены
     *
     * @return
     */
    @Override
    public List<AbstractWishDto> getOwmWishesInProgress() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        List<Wish> ownWishes = wishRepository.findByOwnProfileIdAndStatusIn(currentProfile.getId(),
                List.of(WishStatus.NEW, WishStatus.IN_PROGRESS));

        return convertIfWishInProgress(ownWishes);
    }

    //возвращает собственное желание в любом статусе
    // принадлежит ли это желание текущему профилю
    @Override
    public AbstractWishDto getOwmWish(long wishId) {
        return null;
    }


    /**
     * фильтр желаний для других пользователей происходит по статусу new
     * Нужно установить значения для  sql.Date fromDate и sql.Date toDate для дальнейшей фильтрации в репозитории
     * в формате yyyy-mm-dd как в базе данных, если в реквесте есть мин и мах возраст
     *
     * @param pageable
     * @param request
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public Page<SearchWishDto> find(Pageable pageable, WishSearchRequest request) {
        request.setStatus(WishStatus.NEW);

        if (request.getTitle() == null &&
                request.getPriorities() == null &&
                request.getProfileSex() == null &&
                request.getCountry() == null &&
                request.getMinAge() == null &&
                request.getMaxAge() == null &&
                request.getTags() == null) {
            Page<Wish> all = wishRepository.findByStatus(WishStatus.NEW, pageable);

            Stream<SearchWishDto> searchWishDtoStream = all.getContent().stream().map(wishMapper::convertToSearchDto);

            return new PageImpl<>(searchWishDtoStream.collect(Collectors.toList()), pageable, all.getTotalElements());
        }

        if (request.getMinAge() != null && request.getMaxAge() == null) {
            request.setFromDate(DateUtil.getDate(100));
            request.setToDate(DateUtil.getDate(request.getMinAge()));
        }
        if (request.getMinAge() == null && request.getMaxAge() != null) {
            request.setFromDate(DateUtil.getDate(request.getMaxAge()));
            request.setToDate(DateUtil.getDate(10));
        }
        if (request.getMinAge() != null && request.getMaxAge() != null) {
            request.setFromDate(DateUtil.getDate(request.getMaxAge()));
            request.setToDate(DateUtil.getDate(request.getMinAge()));
        }

        Page<Wish> all = wishRepository.findAll(WishRepository.Specs.bySearchRequest(request), pageable);

        Stream<SearchWishDto> searchWishDtoStream = all.getContent().stream().map(wishMapper::convertToSearchDto);

        return new PageImpl<>(searchWishDtoStream.collect(Collectors.toList()), pageable, all.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Override
    public SearchScreenWishDto getSearchScreenWish(Pageable pageable) {

        Set<Tag> tagSet = EnumSet.allOf(Tag.class);
        int countOfWishes = wishRepository.countByStatus(WishStatus.NEW);
        Page<Wish> wishes = wishRepository.findByStatus(WishStatus.NEW, pageable);

        Stream<SearchWishDto> wishDtoStream = wishes.getContent().stream().map(wishMapper::convertToSearchDto);

        SearchScreenWishDto searchScreenWishDto = new SearchScreenWishDto();
        searchScreenWishDto.setWishes(wishDtoStream.collect(Collectors.toList()));
        searchScreenWishDto.setCountAllOfWishes(countOfWishes);
        searchScreenWishDto.setTags(tagSet);

        return searchScreenWishDto;
    }

    /**
     * отображают желание не из истории
     * @param id
     * @return
     */
    @Transactional()
    @Override
    public AbstractWishDto find(long id) {
        Wish wish = wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));

        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId()).get();

        wish.setWatchCount(countProfileVisitorWish(profile, wish));

        if (wish.getStatus() == WishStatus.NEW) {
            return wishMapper.convertToDto(wish);
        } else if (wish.getStatus() == WishStatus.IN_PROGRESS) {
            return wishMapper.convertToDto(executingWishRepository.findByWishId(id).get());
        }

//        } else if (wish.getStatus() == WishStatus.FINISHED) {
//            FinishedWish finishedWish = finishedWishRepository.findByWishId(id).get();
//            return wishMapper.convertToDto(finishedWish);
//        }
        //удаленные желания не должны быть доступны никаким profile
        throw new WishException("the wish has been deleted and is not available");
    }

    @Override
    public MainScreenProfileDto execute(long wishId, Boolean anonymously) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        if (wish.getOwnProfile().getId() == profile.getId()) throw new ProfileException("can't execute own wish");

        if (wish.getStatus() == WishStatus.IN_PROGRESS) throw new WishException("wish is already executing");


        wish.setStatus(WishStatus.IN_PROGRESS);

        ExecutingWish executingWish = new ExecutingWish(wish);

        if (anonymously) {
            executingWish.setExecutingStatus(ExecuteStatus.IN_PROGRESS_ANONYMOUS);
        } else {
            executingWish.setExecutingStatus(ExecuteStatus.IN_PROGRESS);
        }
        executingWish.setExecutingProfile(profile);

        Timestamp now = Timestamp.from(Instant.now());
        Timestamp result = Timestamp.from(now.toInstant().plus(MAX_COUNT_OF_DAYS_TO_FINISH_WISH, ChronoUnit.DAYS));
        executingWish.setFinish(result);

        executingWishRepository.save(executingWish);

        // notificationManagerService.sendExecuteWishToExecutor(executingWish.getExecutingProfile());
        //   notificationManagerService.sendExecuteWishToOwner(wish.getOwnProfile());

        return profileDtoBuilder.buildMainScreen(profileRepository.findById(profile.getId()).get());
    }

    @Transactional(readOnly = true)
    @Override
    public StoryWishDto getStoryWish() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        StoryWishDto storyWish = new StoryWishDto();
        List<FinishedWish> finishedWishesAnother = finishedWishRepository
                .findByStatusAndExecutedProfileId(FinishWishStatus.FINISHED_SUCCESS, currentProfile.getId());
        List<FinishedWish> finishedAnonymousWishesAnother = finishedWishRepository
                .findByStatusAndExecutedProfileId(FinishWishStatus.FINISHED_SUCCESS_ANONYMOUS, currentProfile.getId());
        finishedWishesAnother.addAll(finishedAnonymousWishesAnother);

        List<FinishedWish> notFinishedWishesAnother = finishedWishRepository
                .findByStatusAndExecutedProfileId(FinishWishStatus.FINISHED_FAILED, currentProfile.getId());
        List<FinishedWish> notFinishedAnonymousWishesAnother = finishedWishRepository
                .findByStatusAndExecutedProfileId(FinishWishStatus.FINISHED_FAILED_ANONYMOUS, currentProfile.getId());
        notFinishedWishesAnother.addAll(notFinishedAnonymousWishesAnother);

        List<FinishedWish> ownFinished = finishedWishRepository.findByWishOwnProfileId(currentProfile.getId());
        List<Wish> ownWishesDeleted = wishRepository.findByOwnProfileIdAndStatus(currentProfile.getId(), WishStatus.DELETED);

        storyWish.setAnotherFinishedWishes(finishedWishesAnother.stream().map(wishMapper::convertToDto).collect(Collectors.toList()));
        storyWish.setAnotherNotFinishedWishes(notFinishedWishesAnother.stream().map(wishMapper::convertToDto).collect(Collectors.toList()));

        List<FinishedWishDto> collect = ownFinished.stream().map(wishMapper::convertToDto).collect(Collectors.toList());
        List<AbstractWishDto> collect1 = ownWishesDeleted.stream().map(wishMapper::convertToDto).collect(Collectors.toList());
        collect1.addAll(collect);

        storyWish.setOwnWishes(collect1);

        return storyWish;
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

    private void validateNewWish(Profile profile, Wish wish) {
        if (!profile.getOwnWishes().isEmpty()) {

            if (profile.getOwnWishes().size() >= MAX_COUNT_WISHES)
                throw new MaxCountOfWishesException("too much wishes. You have " + MAX_COUNT_WISHES + " wishes");

            if (profile.getOwnWishes().contains(wish))
                throw new TheSameWishExistsException("the same wish");

            validateByPriority(profile, wish);
        }
    }

    private void validateByPriority(Profile profile, Wish wish) {
        for (Wish w : profile.getOwnWishes()) {
            if (Objects.equals(w.getId(), wish.getId())) continue;
            if (w.getPriority() == wish.getPriority())
                throw new PriorityWishException("You have a wish of the same priority " + w.getPriority());
        }
    }

    private int countProfileVisitorWish(Profile profile, Wish wish) {
        int watchCount = 0;
        if (!(profile == wish.getOwnProfile())) {
            ProfileVisitorWish profileVisitorWish = new ProfileVisitorWish(profile, wish);
            try {
                profileVisitorService.save(profileVisitorWish);
                watchCount = profileVisitorService.countProfilesByWish(wish);
            } catch (Exception ex) {
                LOGGER.info("Failed to save profileVisitorWish: " + ex.getMessage());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        } else {
            watchCount = profileVisitorService.countProfilesByWish(wish);
        }
        return watchCount;
    }

}
