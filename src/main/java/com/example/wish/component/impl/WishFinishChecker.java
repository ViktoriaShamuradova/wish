package com.example.wish.component.impl;

import com.example.wish.component.NotificationManagerService;
import com.example.wish.entity.*;
import com.example.wish.repository.ExecutingWishRepository;
import com.example.wish.repository.FinishedWishRepository;
import com.example.wish.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;


@RequiredArgsConstructor
@Component
public class WishFinishChecker {

    private final WishRepository wishRepository;
    private final FinishedWishRepository finishedWishRepository;
    private final ExecutingWishRepository executingWishRepository;
    private final NotificationManagerService notificationManagerService;

    @Transactional
    @Scheduled(fixedRateString = "PT3M")
    public void changeStatus() {

        List<ExecutingWish> byFinishBefore = executingWishRepository.findByFinishBefore(Timestamp.from(Instant.now()));

        for (ExecutingWish executingWish : byFinishBefore) {
            if (executingWish.getExecutingStatus() == ExecuteStatus.IN_PROGRESS_ANONYMOUS
                    || executingWish.getExecutingStatus() == ExecuteStatus.IN_PROGRESS) {
                handleNotExecutedWish(executingWish);
            } else if (executingWish.getExecutingStatus() == ExecuteStatus.WAITING_FOR_CONFIRMATION
                    || executingWish.getExecutingStatus() == ExecuteStatus.WAITING_FOR_CONFIRMATION_ANONYMOUS) {
                handleNotExecutedWish(executingWish);
            }
        }
    }

    private void handleNotExecutedWish(ExecutingWish executingWish) {

        FinishedWish finishedWish = new FinishedWish();
        finishedWish.setExecutedProfile(executingWish.getExecutingProfile());
        finishedWish.setWish(executingWish.getWish());
        finishedWish.setFinish(Timestamp.from(Instant.now()));
        finishedWish.setStatus(FinishWishStatus.FINISHED_FAILED);

        finishedWishRepository.save(finishedWish);
        executingWishRepository.delete(executingWish);
        Wish wish = wishRepository.findById(executingWish.getWish().getId()).get();
        wish.setStatus(WishStatus.NEW);
        wishRepository.save(wish);


        notificationManagerService.sendFinishFailedWishToExecutor(executingWish);
        notificationManagerService.sendFinishFailedWishToOwner(executingWish);
    }
}
