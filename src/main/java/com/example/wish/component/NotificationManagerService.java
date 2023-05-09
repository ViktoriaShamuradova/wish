package com.example.wish.component;

import com.example.wish.entity.ConfirmationToken;
import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.Profile;

public interface NotificationManagerService {

    void sendConfirmationTokenForRegistration(Profile profile, ConfirmationToken confirmationToken);


    void sendExecuteWishToOwner(Profile ownProfile);

    void sendExecuteWishToExecutor(Profile executingProfile);

    void sendExecuteWishToOwnerToConfirm(Profile ownProfile, ExecutingWish executingWish);

    void sendFinishFailedWishToExecutor(ExecutingWish executingWish);

    void sendFinishFailedWishToOwner(ExecutingWish executingWish);

    void sendConfirmWishSuccessToExecutor(Profile executingProfile);

    void sendConfirmWishSuccessToOwner(Profile ownProfile);

    void sendCancelExecutionToExecutor(Profile executingProfile);

    void sendCancelExecutionToOwner(Profile ownProfile);

    void sendConfirmWishFailedToExecutor(Profile executingProfile);

    void sendConfirmWishFailedToOwner(Profile ownProfile);

    void sendConfirmationTokenForPassword(Profile profile, ConfirmationToken confirmationToken, int minutes);

    void sendOnePasswordForEmailVerification(String email, String oneTimePassword, int minutes);
}
