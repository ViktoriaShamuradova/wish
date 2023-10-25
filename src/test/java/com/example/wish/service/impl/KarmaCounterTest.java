package com.example.wish.service.impl;

import com.example.wish.component.impl.KarmaCounterImpl;
import com.example.wish.entity.*;
import com.example.wish.repository.FinishedWishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KarmaCounterTest {

    @Mock
    private FinishedWishRepository finishedWishRepository;

    @InjectMocks
    private KarmaCounterImpl karmaCounter;

    private static final long OWN_PROFILE_ID = 1L;
    private static final long EXECUTOR_PROFILE_ID = 2L;
    private static final long WISH_ID = 1L;

    @Test
    public void test_countSuccess() {
        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        Profile executorProfile = createProfile(EXECUTOR_PROFILE_ID);
        Wish wish = createWish(Priority.ONE, ownProfile);
        ExecutingWish executeWish = createExecuteWish(ExecuteStatus.WAITING_FOR_CONFIRMATION,
                wish, executorProfile);

        //то есть 1 раз исполнял
        List<FinishedWish> finishedWishList = List.of(createListFinishedWish(FinishWishStatus.FINISHED_SUCCESS, executorProfile, 0, wish, false));

        when(finishedWishRepository.findByStatusAndWishOwnProfileId(FinishWishStatus.FINISHED_SUCCESS, ownProfile.getId()))
                .thenReturn(finishedWishList);

        //Priority.ONE - 7, 1 - 50%: (7*50)/100
        karmaCounter.count(executeWish);

        assertEquals(3.5, executorProfile.getKarma());
    }

    private Profile createProfile(long profileId) {
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setFirstName("firstname");
        profile.setLastName("lastname");
        profile.setEmail("user@email.com");
        profile.setPassword("password");
        profile.setEnabled(true);
        profile.setLocked(false);
        profile.setStatus(ProfileStatus.RED);
        profile.setStatusLevel(ProfileStatusLevel.PERSON);

        return profile;
    }

    private Wish createWish(Priority priority, Profile ownProfile) {
        Wish wish = new Wish();
        wish.setTitle("Title");
        wish.setDescription("Desc");

        Tag t = new Tag();
        t.setTagName(TagName.MONEY);

        wish.setTags(Set.of(t));
        wish.setId(WISH_ID);
        wish.setStatus(WishStatus.IN_PROGRESS);

        wish.setPriority(priority);

        wish.setOwnProfile(ownProfile);

        return wish;
    }

    private ExecutingWish createExecuteWish(ExecuteStatus status, Wish wish, Profile executorProfile) {
        ExecutingWish executingWish = new ExecutingWish();
        executingWish.setId(1L);
        executingWish.setWish(wish);
        executingWish.setExecutingProfile(executorProfile);
        executingWish.setExecutingStatus(status);
        executingWish.setFinish(Date.from(Instant.now()));
        return executingWish;
    }


    private FinishedWish createListFinishedWish(FinishWishStatus status, Profile executeProfile, int attempts, Wish wish, boolean anonymously) {
        FinishedWish finishedWish = new FinishedWish();
        finishedWish.setWish(wish);
        finishedWish.setAttempts(attempts);
        finishedWish.setStatus(status);
        finishedWish.setExecutedProfile(executeProfile);
        finishedWish.setAnonymously(anonymously);

        return finishedWish;
    }
}
