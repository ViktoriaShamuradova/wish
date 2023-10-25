package com.example.wish.component;

import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.Priority;
import com.example.wish.entity.Profile;

public interface KarmaCounter {

    void count(ExecutingWish executingWish);

    int transformIntoKarma(Priority priority);

    double calculateAdditionalSumToKarma(ExecutingWish executingWish);

    void changeStatus(Profile executedProfile);
}
