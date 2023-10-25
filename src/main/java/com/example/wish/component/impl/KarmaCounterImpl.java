package com.example.wish.component.impl;

import com.example.wish.component.KarmaCounter;
import com.example.wish.entity.*;
import com.example.wish.repository.FinishedWishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class KarmaCounterImpl implements KarmaCounter {
    private final Map<Integer, Double> subtractionPercentage = new HashMap<>();
    private final Map<Priority, Integer> karmaValue = new HashMap<>();
    private final Map<Integer, Map<ProfileStatus, ProfileStatusLevel>> profileLevels = new TreeMap<>();

    @Autowired
    private final FinishedWishRepository finishedWishRepository;

    public KarmaCounterImpl(FinishedWishRepository finishedWishRepository) {
        this.finishedWishRepository = finishedWishRepository;
        subtractionPercentage.put(0, 100.0); //если никогда не исполнял, то начисляем 100 от приоритета
        subtractionPercentage.put(1, 50.0); //один раз уже исполнил
        subtractionPercentage.put(2, 10.0); //два раза исполнил

        karmaValue.put(Priority.ONE, 7); //в зависимости от приоритета начисляем карму
        karmaValue.put(Priority.TWO, 6);
        karmaValue.put(Priority.THREE, 5);
        karmaValue.put(Priority.FOUR, 4);
        karmaValue.put(Priority.FIVE, 3);
        karmaValue.put(Priority.SIX, 2);
        karmaValue.put(Priority.SEVEN, 1);


        profileLevels.put(0, Map.of(ProfileStatus.RED, ProfileStatusLevel.PERSON)); //в зависимости от суммы кармы меняется статус у профиля
        profileLevels.put(1, Map.of(ProfileStatus.ORANGE, ProfileStatusLevel.PERSON));
        profileLevels.put(2, Map.of(ProfileStatus.YELLOW, ProfileStatusLevel.PERSON));
        profileLevels.put(3, Map.of(ProfileStatus.GREEN, ProfileStatusLevel.PERSON));
        profileLevels.put(5, Map.of(ProfileStatus.LIGHT_BLUE, ProfileStatusLevel.PERSON));
        profileLevels.put(8, Map.of(ProfileStatus.DARK_BLUE, ProfileStatusLevel.PERSON));
        profileLevels.put(13, Map.of(ProfileStatus.PURPLE, ProfileStatusLevel.PERSON));

        profileLevels.put(21, Map.of(ProfileStatus.ANGEL, ProfileStatusLevel.THIRD_HIERARCHY));
        profileLevels.put(34, Map.of(ProfileStatus.ARCHANGEL, ProfileStatusLevel.THIRD_HIERARCHY));
        profileLevels.put(55, Map.of(ProfileStatus.BEGINNING, ProfileStatusLevel.THIRD_HIERARCHY));

        profileLevels.put(88, Map.of(ProfileStatus.POWER, ProfileStatusLevel.SECOND_HIERARCHY));
        profileLevels.put(144, Map.of(ProfileStatus.FORCE, ProfileStatusLevel.SECOND_HIERARCHY));
        profileLevels.put(233, Map.of(ProfileStatus.DOMINATION, ProfileStatusLevel.SECOND_HIERARCHY));

        profileLevels.put(377, Map.of(ProfileStatus.THRONE, ProfileStatusLevel.FIRST_HIERARCHY));
        profileLevels.put(610, Map.of(ProfileStatus.CHERUB, ProfileStatusLevel.FIRST_HIERARCHY));
        profileLevels.put(987, Map.of(ProfileStatus.SERAPHIM, ProfileStatusLevel.FIRST_HIERARCHY));

        profileLevels.put(1597, Map.of(ProfileStatus.GOD, ProfileStatusLevel.GOD));

    }

    @Override
    public void count(ExecutingWish executingWish) {

        double additionalSum = calculateAdditionalSumToKarma(executingWish);
        double result = executingWish.getExecutingProfile().getKarma() + additionalSum;

        executingWish.getExecutingProfile().setKarma(result);
    }

    @Override
    public double calculateAdditionalSumToKarma(ExecutingWish executingWish) {
        int countOfExecution = getCountOfFinishedWishesForTheSameOwnProfile(executingWish);
        return calculateAdditionalSumToKarma(executingWish, countOfExecution);
    }

    private double calculateAdditionalSumToKarma(ExecutingWish executingWish, int countOfExecution) {
        double additionalSum = 0;
        if (subtractionPercentage.containsKey(countOfExecution)) {
            additionalSum = karmaValue.get(executingWish.getWish().getPriority()) * subtractionPercentage.get(countOfExecution) / 100;
        } //если нету в мэпе, значит больше 2ух раз исполнял желание и начисление составляет 0

        return additionalSum;
    }

    @Override
    public int transformIntoKarma(Priority priority) {
        if (karmaValue.containsKey(priority)) {
            return karmaValue.get(priority);
        }
        return 0;
    }

    @Override
    public void changeStatus(Profile executedProfile) {
        double karma = executedProfile.getKarma();
        int karmaInt = (int) karma;

        Map.Entry<Integer, Map<ProfileStatus, ProfileStatusLevel>> statusEntry = null;
        for (Map.Entry<Integer, Map<ProfileStatus, ProfileStatusLevel>> profileLevelEntry : profileLevels.entrySet()) {

            if (profileLevelEntry.getKey() == karmaInt) {
                statusEntry = profileLevelEntry;
                break;
            }
            if (profileLevelEntry.getKey() < karmaInt) {
                statusEntry = profileLevelEntry;
            }
        }

        executedProfile.setStatus(statusEntry.getValue().keySet().stream().findFirst().get());
        executedProfile.setStatusLevel(statusEntry.getValue().values().stream().findFirst().get());
    }

    //ищем сколько раз исполняющий профиль выполнял желания данного собственника
    private int getCountOfFinishedWishesForTheSameOwnProfile(ExecutingWish executingWish) {

        List<FinishedWish> finishedWishes = finishedWishRepository.findByStatusAndWishOwnProfileId(FinishWishStatus.FINISHED_SUCCESS,
                executingWish.getWish().getOwnProfile().getId());
        return finishedWishes.size();
    }
}