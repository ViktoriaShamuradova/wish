package com.example.wish.service.impl;

import com.example.wish.component.KarmaCounter;
import com.example.wish.component.NotificationManagerService;
import com.example.wish.component.WishMapper;
import com.example.wish.dto.SearchScreenWishDto;
import com.example.wish.dto.wish.*;
import com.example.wish.entity.*;
import com.example.wish.entity.meta_model.Wish_;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.wish.*;
import com.example.wish.model.CurrentProfile;
import com.example.wish.model.search_request.WishSearchRequest;
import com.example.wish.repository.*;
import com.example.wish.service.ProfileVisitorService;
import com.example.wish.service.WishImageService;
import com.example.wish.service.WishService;
import com.example.wish.util.DateUtil;
import com.example.wish.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
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

    private final NotificationManagerService notificationManagerService;
    private final AuthenticationService authenticationService;
    private final KarmaCounter karmaCounter;
    private final ProfileVisitorService profileVisitorService;
    private final WishImageService wishImageService;
    private final TagRepository tagRepository;

    @Value("${wish.max.count}")
    private int wishMaxCount;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;
    private static final long MAX_COUNT_OF_DAYS_TO_FINISH_WISH = 7;
    private static final long MAX_COUNT_OF_DAYS_TO_CONFIRM_WISH = 7;

    @Transactional
    @Override
    public WishDto create(CreateWishRequest createWishRequest) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishMapper.convertToEntity(createWishRequest);

        checkProfileCanCreateWish(profile, wish);

        wish.setStatus(WishStatus.NEW);
        wish.setCreated(Timestamp.from(Instant.now()));

        Set<Tag> tags = createOrUpdateTags(createWishRequest.getTagNames());

        wish.setTags(tags);

        profile.addInOwnWishes(wish);

        return wishMapper.convertToDto(wishRepository.save(wish));
    }


    @Transactional
    private Set<Tag> createOrUpdateTags(Set<TagName> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null) {
            for (TagName tagName : tagNames) {
                // Check if the tag already exists in the database
                Tag existingTag = tagRepository.findByTagName(tagName);
                if (existingTag != null) {
                    tags.add(existingTag);
                } else {
                    // If the tag doesn't exist, create and save it
                    Tag newTag = new Tag();
                    newTag.setTagName(tagName);
                    tags.add(tagRepository.save(newTag));
                }
            }
        }
        return tags;
    }


    @Transactional
    @Override
    public void uploadImage(Long wishId, MultipartFile file) throws IOException, HttpMediaTypeNotSupportedException {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        ImageUtil.checkImage(file, maxSize);
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        if (wish.getOwnProfile().getId() != currentProfile.getId())
            throw new WishException("can't upload image not own wish");

        if (wish.getImage() != null) wishImageService.delete(wish.getImage());
        wish.setImage(wishImageService.save(file));
    }

    /**
     * @param wishId
     * @throws HttpMediaTypeNotSupportedException
     * @throws IOException
     */
    @Transactional(readOnly = true)
    @Override
    public byte[] findImage(long wishId) {
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        if (wish.getImage() == null) return null;

        return wish.getImage().getImageData();
    }


    private void checkProfileCanCreateWish(Profile profile, Wish wish) {
        if (!profile.getOwnWishes().isEmpty()) {

            if (profile.getOwnWishes().size() >= wishMaxCount)
                throw new MaxCountOfWishesException("too much wishes. You have " + wishMaxCount + " wishes");

            if (profile.getOwnWishes().contains(wish))
                throw new TheSameWishExistsException("the same wish");

            validateByPriority(profile.getOwnWishes(), wish.getPriority());
        }
    }

    @Transactional
    @Override
    public WishDto update(long id, UpdateWishRequest wishDto) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));
        validateWishUpdate(profile, wish);
        validateByPriority(profile.getOwnWishes(), wishDto.getPriority());

        wish.setTitle(wishDto.getTitle());
        wish.setDescription(wishDto.getDescription());
        wish.setPriority(wishDto.getPriority());

        Set<Tag> tags = createOrUpdateTags(wishDto.getTagNames());
        wish.setTags(tags);

        return wishMapper.convertToDto(wishRepository.save(wish));
    }


    @Transactional
    @Override
    public WishDto updateByFields(long id, Map<String, Object> fields) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));

        validateWishUpdate(profile, wish);
        checkFieldsToUpdate(fields, wish);

        if (fields.containsKey(Wish_.PRIORITY)) {
            Object priorityValue = fields.get(Wish_.PRIORITY);
            Priority updatedPriority = Priority.fromString(priorityValue.toString());
            validateByPriority(profile.getOwnWishes(), updatedPriority);
        }

        try {
            fields.forEach((key, value) -> {
                Field field;
                if ("tagNames".equals(key)) {
                    field = ReflectionUtils.findField(Wish.class, "tags");
                    assert field != null;
                    field.setAccessible(true);
                } else {
                    field = ReflectionUtils.findField(Wish.class, key);
                    assert field != null;
                    field.setAccessible(true);
                }
                if ("tagNames".equals(key)) {
                    Set<Tag> orUpdateTags = createOrUpdateTags(convertIntoTagNames(value));
                    wish.setTags(orUpdateTags);
                } else if (field.getType().isEnum()) {
                    setEnumField(field, wish, value.toString());
                } else {
                    ReflectionUtils.setField(field, wish, value);
                }
            });
            return wishMapper.convertToDto(wishRepository.save(wish));
        } catch (Exception e) {
            log.error("Error updating wish with ID: {}", id, e);
            throw new WishException("Error updating wish. " + e.getMessage(), e);
        }
    }

    private Set<TagName> convertIntoTagNames(Object value) {
        if (value instanceof List) {
            Set<TagName> newTags = new HashSet<>();
            for (Object tag : (List<?>) value) {
                if (tag instanceof String) {
                    try {
                        TagName tagName = TagName.valueOf((String) tag);
                        newTags.add(tagName);
                    } catch (IllegalArgumentException e) {
                        throw new WishException("Invalid tag name: " + tag);
                    }
                } else {
                    throw new WishException("Invalid value type for tags field.");
                }
            }
            return newTags;
        } else {
            throw new WishException("Invalid value for tags field.");
        }
    }


    private void validateWishUpdate(Profile profile, Wish wish) {
        if (wish.getOwnProfile().getId() != profile.getId())
            throw new WishException("Profile cannot update wish because it does not belong to their ownWishes list.");

        if (wish.getStatus() == WishStatus.IN_PROGRESS || wish.getStatus() == WishStatus.FINISHED)
            throw new WishException("Can't update the wish until it's in progress");

    }

    private void setEnumField(Field field, Wish wish, String value) {
        try {
            if (field.getType().isEnum()) {
                Enum<?> enumValue;

                if (field.getType().equals(Priority.class)) {
                    enumValue = Priority.fromString(value);
                } else {
                    // For other enums, convert String value to the corresponding enum
                    enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), value);
                }
                ReflectionUtils.setField(field, wish, enumValue);
            } else {
                throw new IllegalArgumentException("Field is not of enum type: " + field.getName());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value for field: " + field.getName());
        }
    }

    private void checkFieldsToUpdate(Map<String, Object> fields, Wish wish) {
        UpdateWishRequest updateWishRequest = new UpdateWishRequest();
        Class<?> updateWishClass = updateWishRequest.getClass();

        for (String key : fields.keySet()) {

            try {
                Field updateDetailsField = updateWishClass.getDeclaredField(key); //здесь выбрасывается исключение
                updateDetailsField.setAccessible(true);
                updateDetailsField.get(updateWishRequest);


            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new WishException("Unable to update field= " + e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public void finish(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));

        ExecutingWish executingWish = executingWishRepository.findByWishId(id).orElseThrow(()
                -> new WishException("try to finish not executing wish"));//no wish in the table

        if (executingWish.getExecutingProfile().getId() != currentProfile.getId())
            throw new WishException("try to finish another's wish");

        if (executingWish.getExecutingStatus() == ExecuteStatus.IN_PROGRESS) {
            executingWish.setExecutingStatus(ExecuteStatus.WAITING_FOR_CONFIRMATION);
        }

        Date finish = executingWish.getFinish();
        finish = Timestamp.from(finish.toInstant().plus(MAX_COUNT_OF_DAYS_TO_CONFIRM_WISH, ChronoUnit.DAYS));
        executingWish.setFinish(finish);
        executingWishRepository.save(executingWish);

        notificationManagerService.sendExecuteWishToOwnerToConfirm(executingWish.getWish().getOwnProfile(), executingWish);
    }

    /**
     * запрос на confirm идет строго после finish
     * чтобы проверить последовательность запросов, нужно проверить желание на стутус waiting_confirmation
     *
     * @param confirmWishRequest
     * @return
     */
    @Transactional
    @Override
    public void confirm(ConfirmWishRequest confirmWishRequest) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        ExecutingWish executingWish = executingWishRepository.findByWishId(confirmWishRequest.getWishId())
                .orElseThrow(() -> new WishNotFoundException(confirmWishRequest.getWishId()));

        validateWishBeforeConfirming(executingWish, currentProfile);

        if (confirmWishRequest.getIsConfirm().equals(true)) {
            confirmProcess(executingWish);
        } else {
            notConfirmProcess(executingWish, confirmWishRequest);
        }
    }

    private void validateWishBeforeConfirming(ExecutingWish executingWish, CurrentProfile currentProfile) {
        //check wish for own profile
        if (executingWish.getWish().getOwnProfile().getId() != currentProfile.getId())
            throw new WishException("can't confirm another wish");
        if (executingWish.getExecutingStatus() != ExecuteStatus.WAITING_FOR_CONFIRMATION) {
            throw new WishException(("executing profile hasn't finish wish yet. Status of wish=" + executingWish.getExecutingStatus()));
        }
    }

    private void notConfirmProcess(ExecutingWish executingWish, ConfirmWishRequest confirmWishRequest) {
        FinishedWish finishedWish = new FinishedWish(executingWish.getExecutingProfile(),
                executingWish.getWish(),
                Timestamp.from(Instant.now()),
                FinishWishStatus.FINISHED_FAILED,
                confirmWishRequest.getReasonOfFailedFromOwner(), executingWish.getAttempt());

        finishedWishRepository.save(finishedWish);
        executingWishRepository.delete(executingWish);

        executingWish.getWish().setStatus(WishStatus.NEW);
        wishRepository.save(executingWish.getWish());

        notificationManagerService.sendConfirmWishFailedToExecutor(executingWish.getExecutingProfile());
        notificationManagerService.sendConfirmWishFailedToOwner(executingWish.getWish().getOwnProfile());
    }

    private void confirmProcess(ExecutingWish executingWish) {
        FinishedWish finishedWish = new FinishedWish(executingWish.getExecutingProfile(),
                executingWish.getWish(),
                Timestamp.from(Instant.now()),
                FinishWishStatus.FINISHED_SUCCESS, executingWish.getAttempt());

        karmaCounter.count(executingWish);
        karmaCounter.changeStatus(executingWish.getExecutingProfile());

        finishedWish.setEarnKarma(karmaCounter.calculateAdditionalSumToKarma(executingWish));
        finishedWishRepository.save(finishedWish);
        executingWishRepository.delete(executingWish);

        executingWish.getWish().setStatus(WishStatus.FINISHED);
        wishRepository.save(executingWish.getWish());

        notificationManagerService.sendConfirmWishSuccessToExecutor(executingWish.getExecutingProfile());
        notificationManagerService.sendConfirmWishSuccessToOwner(executingWish.getWish().getOwnProfile());
    }

    /**
     * нельзя удалять желание, если оно в процессе выполнения
     * удаляем картинку
     * удалять теги? пока теги удаляются
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public void delete(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        if (wish.getOwnProfile().getId() != currentProfile.getId())
            throw new WishException("can't delete not own wish");
        if (wish.getStatus() == WishStatus.IN_PROGRESS) throw new WishException("can't delete in progress wish");
        wishRepository.delete(wish);
    }

    @Transactional
    @Override
    public void deleteExecutingWishes(Profile executedProfile) {
        List<ExecutingWish> executingWishes = executingWishRepository.findByExecutingProfileId(executedProfile.getId());
        for (ExecutingWish executingWish : executingWishes) {
            executingWish.getWish().setStatus(WishStatus.NEW);
        }
        executingWishRepository.deleteByExecutingProfile(executedProfile);

    }

    @Override
    public void cancelExecution(long id) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Wish wish = wishRepository.findById(id)
                .orElseThrow(() -> new WishNotFoundException(id));

        ExecutingWish executingWish = executingWishRepository.findByWishId(id)
                .orElseThrow(() -> new WishException("you do not execute this wish"));

        if (executingWish.getExecutingProfile().getId() != currentProfile.getId())
            throw new WishException("this wish not your executing wish");

        FinishedWish finishedWish = new FinishedWish();
        finishedWish.setFinish(Timestamp.from(Instant.now()));
        if (executingWish.isAnonymously()) {
            finishedWish.setAnonymously(true);
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

    }

    /**
     * get own wishes in progress or new main screen
     * - проверить, чтобы не возвращали желания, которые уже завершены - не возвращает. написать для этого тесты
     *
     * @return
     */
    @Override
    public List<AbstractWishDto> getOwmWishesMainScreen() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        List<Wish> ownWishes = wishRepository.findByOwnProfileIdAndStatusIn(currentProfile.getId(),
                List.of(WishStatus.NEW, WishStatus.IN_PROGRESS));

        return convertIfWishInProgress(ownWishes);
    }

    /**
     * Возвращает другие желания, который текущий юзер исполняет  для главного экрана
     * статус in-progress, finish_failed - например возвращать определенное количество дней. Далее помещается в историю
     * in_progress
     *
     * @return
     */
    @Override
    public List<AbstractWishDto> getExecutingWishesMainScreen() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        List<AbstractWishDto> executingWishesDto = findExecutingWishesDto(currentProfile.getId());
        List<AbstractWishDto> finishedFailedWishDto =
                findFinishedWishDto(List.of(FinishWishStatus.FINISHED_FAILED), currentProfile.getId());

        executingWishesDto.addAll(finishedFailedWishDto);
        return executingWishesDto;
    }


    private List<AbstractWishDto> findFinishedWishDto(List<FinishWishStatus> statuses, long profileId) {
        return finishedWishRepository.findByExecutedProfileIdAndStatusIn(profileId, statuses)
                .stream()
                .map(wishMapper::convertToDto)
                .collect(Collectors.toList());
    }


    private List<AbstractWishDto> findExecutingWishesDto(Long profileId) {
        return executingWishRepository.findByExecutingProfileId(profileId)
                .stream()
                .map(wishMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AbstractWishDto> getOwnWish(WishStatus wishStatus) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        return wishRepository.findByOwnProfileIdAndStatus(currentProfile.getId(), wishStatus)
                .stream()
                .map(wishMapper::convertToDto)
                .collect(Collectors.toList());
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

        Set<TagName> tagSet = EnumSet.allOf(TagName.class);
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
     * используется для просмотра другим юзером и текущим.
     * Для текущего возвращаюся желания в статусе new, in-progress, finished
     * Для другого возвращаюся желания в статусе new, in-progress
     *
     * @param id
     * @return
     */
    @Transactional()
    @Override
    public AbstractWishDto find(long id) {
        Wish wish = wishRepository.findById(id).orElseThrow(() -> new WishNotFoundException(id));

        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId()).get();

        if (wish.getOwnProfile().getId().equals(profile.getId())) {
            return mapWishBasedOnStatus(wish);
        } else {
            if (wish.getStatus() == WishStatus.NEW || wish.getStatus() == WishStatus.IN_PROGRESS) {
                wish.setWatchCount(countProfileVisitorWish(profile, wish));
                return mapWishBasedOnStatus(wish);
            }
            throw new WishException("This wish is not available for viewing by another profile. Wish in status " + wish.getStatus());
        }
    }

    private AbstractWishDto mapWishBasedOnStatus(Wish wish) {
        if (wish.getStatus() == WishStatus.NEW) {
            return wishMapper.convertToDto(wish);
        } else if (wish.getStatus() == WishStatus.IN_PROGRESS) {
            return wishMapper.convertToDto(executingWishRepository.findByWishId(wish.getId())
                    .orElseThrow(() -> new WishException("Executing wish not found for wish ID: " + wish.getId())));
        } else if (wish.getStatus() == WishStatus.FINISHED) {
            return wishMapper.convertToDto(finishedWishRepository.findByWishIdAndStatusIn(wish.getId(),
                            List.of(FinishWishStatus.FINISHED_SUCCESS))
                    .orElseThrow(() -> new WishException("Finished wish not found for wish ID: " + wish.getId())));
        }

        throw new WishException("This wish is not available by this status= " + wish.getStatus());
    }

    /**
     * исключение - если пытаешься взять на исполнение свое желание - done
     * исключение - пользователь не найден - done
     * исключение - желание на найдено - done
     * исключение - желание в процессе выполнения - done
     * исключение - если количество повыток на выполнение этим юзером уже 3 - done
     */

    @Override
    public void execute(long wishId, Boolean anonymously) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        if (wish.getOwnProfile().getId() == profile.getId()) throw new ProfileException("can't execute own wish");

        if (wish.getStatus() == WishStatus.IN_PROGRESS) throw new WishException("wish is already executing");

        int attempts = getAttemptsNumber(wish, profile);

        ExecutingWish executingWish;

        if (attempts == 3) { //количество попыток не может быть больше 3
            throw new WishException("wish cannot be execute. Number of attempts no more than 3");
        } else {
            executingWish = createExecutingWish(wish, anonymously, attempts, profile);
        }
        executingWishRepository.save(executingWish);

        notificationManagerService.sendExecuteWishToExecutor(executingWish.getExecutingProfile());
        notificationManagerService.sendExecuteWishToOwner(wish.getOwnProfile());
    }

    private ExecutingWish createExecutingWish(Wish wish, Boolean anonymously, int attempts, Profile executingProfile) {
        ExecutingWish executingWish = new ExecutingWish(wish);
        wish.setStatus(WishStatus.IN_PROGRESS);

        executingWish.setExecutingStatus(ExecuteStatus.IN_PROGRESS);
        executingWish.setAttempt(attempts + 1);
        executingWish.setAnonymously(anonymously);
        executingWish.setExecutingProfile(executingProfile);

        Timestamp now = Timestamp.from(Instant.now());
        Timestamp result = Timestamp.from(now.toInstant().plus(MAX_COUNT_OF_DAYS_TO_FINISH_WISH, ChronoUnit.DAYS));
        executingWish.setFinish(result);

        return executingWish;
    }

    //возвращает количество попыток выполнения желания. 0 - если желания нет в финищ таблице
    // проверить здесь на статус и исключение логической ошибки
    private int getAttemptsNumber(Wish wish, Profile profile) {
        List<FinishedWish> finishedWishes = finishedWishRepository.findByWishAndExecutedProfile(wish, profile);
        int attempts = 0;
        if (!finishedWishes.isEmpty()) {
            for (FinishedWish finishedWish : finishedWishes) {
                if (finishedWish.getStatus() == FinishWishStatus.FINISHED_SUCCESS) {
                    throw new RuntimeException("logic exception. status must be failed."); //может выбраситься, когда статус основной новый, а почему-то в финиш таблице статус завершен
                }
                attempts = attempts + finishedWish.getAttempts();
            }
        }
        return attempts;
    }

    @Transactional(readOnly = true)
    @Override
    public StoryWishDto getStoryWish() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        StoryWishDto storyWish = new StoryWishDto();
        List<FinishedWish> finishedWishesAnother = finishedWishRepository
                .findByExecutedProfileIdAndStatusIn(currentProfile.getId(),
                        List.of(FinishWishStatus.FINISHED_SUCCESS)
                );


        List<FinishedWish> notFinishedWishesAnother = finishedWishRepository
                .findByExecutedProfileIdAndStatusIn(currentProfile.getId(),
                        List.of(FinishWishStatus.FINISHED_SUCCESS)
                );


        List<FinishedWish> ownFinished = finishedWishRepository.findByWishOwnProfileId(currentProfile.getId());
        // List<Wish> ownWishesDeleted = wishRepository.findByOwnProfileIdAndStatus(currentProfile.getId(), WishStatus.DELETED);

        storyWish.setAnotherFinishedWishes(finishedWishesAnother.stream().map(wishMapper::convertToDto).collect(Collectors.toList()));
        storyWish.setAnotherNotFinishedWishes(notFinishedWishesAnother.stream().map(wishMapper::convertToDto).collect(Collectors.toList()));

        List<AbstractWishDto> collect = ownFinished.stream().map(wishMapper::convertToDto).collect(Collectors.toList());

        // List<AbstractWishDto> collect1 = ownWishesDeleted.stream().map(wishMapper::convertToDto).collect(Collectors.toList());
        //  collect1.addAll(collect);

        storyWish.setOwnWishes(collect);

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

    private void validateByPriority(List<Wish> wishes, Priority newPriority) {
        for (Wish w : wishes) {
            if (w.getPriority() == newPriority)
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
//    private List<ExecutingWishDto> findExecutingWishesDto(Long profileId) {
//        //ищем желания, которые ты исполняешь
//        List<ExecutingWish> executingWishes = executingWishRepository.findByExecutingProfileId(profileId);
//        List<ExecutingWishDto> executingWishesDto = new ArrayList<>();
//        for (ExecutingWish w : executingWishes) {
//            executingWishesDto.add(wishMapper.convertToDto(w));
//        }
//        return executingWishesDto;
//    }


