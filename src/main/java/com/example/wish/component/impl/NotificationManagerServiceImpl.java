package com.example.wish.component.impl;

import com.example.wish.component.NotificationManagerService;
import com.example.wish.component.NotificationSenderService;
import com.example.wish.component.NotificationTemplateService;
import com.example.wish.entity.ConfirmationToken;
import com.example.wish.entity.ExecuteStatus;
import com.example.wish.entity.ExecutingWish;
import com.example.wish.entity.Profile;
import com.example.wish.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class NotificationManagerServiceImpl implements NotificationManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManagerServiceImpl.class);

    private final NotificationSenderService notificationSenderService;
    private final NotificationTemplateService notificationTemplateService;


    @Override
    public void sendConfirmationTokenForRegistration(Profile profile, ConfirmationToken confirmationToken) {
        LOGGER.debug("Send message to confirm email" + profile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("token", confirmationToken.getToken());

        processNotification(profile, "confirm-email-template.flth", content);
    }

    @Override
    public void sendConfirmationTokenForPassword(Profile profile, ConfirmationToken confirmationToken, int minutes) {
        LOGGER.debug("Send message forgot password" + profile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", profile.getFirstName());
        content.put("token", confirmationToken.getToken());
        content.put("expireAt", minutes);

        processNotification(profile, "forgot-password-template.flth", content);
    }

    @Override
    public void sendOnePasswordForEmailVerification(String email, String oneTimePassword, int minutes) {
        LOGGER.debug("Send message to verify email" + email);

        Map<String, Object> content = new HashMap<>();
        content.put("token", oneTimePassword);
        content.put("expireAt", minutes);

        processNotification(email, "one-time-password-registration-template.flth", content);

    }

    @Override
    public void sendOnePasswordForResetPassword(String email, String otp, int expireMinutesForPassword) {
        LOGGER.debug("Send message to verify email for password reset" + email);

        Map<String, Object> content = new HashMap<>();
        content.put("token", otp);
        content.put("expireAt", expireMinutesForPassword);

        processNotification(email, "one-time-password-reset-password-template.flth", content);
    }

    @Override
    public void sendExecuteWishToOwner(Profile ownProfile) {
        LOGGER.debug("Send to owner information about executing wish" + ownProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", ownProfile.getFirstName());
        content.put("lastName", ownProfile.getLastName());

        processNotification(ownProfile, "execute-wish-owner-template.flth", content);
    }

    @Override
    public void sendExecuteWishToExecutor(Profile executingProfile) {
        LOGGER.debug("Send to executor profile information " + executingProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", executingProfile.getFirstName());
        content.put("lastName", executingProfile.getLastName());

        processNotification(executingProfile, "execute-wish-executor-template.flth", content);
    }

    @Override
    public void sendConfirmWishSuccessToExecutor(Profile executingProfile) {
        LOGGER.debug("Send to executor profile information about karma" + executingProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", executingProfile.getFirstName());

        processNotification(executingProfile, "confirm-wish-executor-success-template.flth", content);

    }

    @Override
    public void sendConfirmWishSuccessToOwner(Profile ownProfile) {
        LOGGER.debug("Send to executor profile information about confirming" + ownProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", ownProfile.getFirstName());

        processNotification(ownProfile, "confirm-wish-owner-success-template.flth", content);

    }

    @Override
    public void sendConfirmWishFailedToExecutor(Profile executingProfile) {
        LOGGER.debug("Send to executor profile information about confirming failed" + executingProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", executingProfile.getFirstName());

        processNotification(executingProfile, "confirm-wish-executor-failed-template.flth", content);
    }

    @Override
    public void sendConfirmWishFailedToOwner(Profile ownProfile) {
        LOGGER.debug("Send to owner profile information about confirming failed" + ownProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", ownProfile.getFirstName());

        processNotification(ownProfile, "confirm-wish-owner-failed-template.flth", content);
    }


    @Override
    public void sendCancelExecutionToExecutor(Profile executingProfile) {
        LOGGER.debug("Send to executor cancel execution's information about confirming" + executingProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        content.put("firstName", executingProfile.getFirstName());

        processNotification(executingProfile, "cancel-execution-wish-executor-template.flth", content);
    }

    @Override
    public void sendCancelExecutionToOwner(Profile ownProfile) {
        LOGGER.debug("Send to owner profile cancel execution's information about confirming" + ownProfile.getEmail());
        Map<String, Object> content = new HashMap<>();
        content.put("firstName", ownProfile.getFirstName());

        processNotification(ownProfile, "cancel-execution-wish-owner-template.flth", content);
    }

    @Override
    public void sendExecuteWishToOwnerToConfirm(Profile ownProfile, ExecutingWish executingWish) {
        LOGGER.debug("Send to owner profile to confirm request " + ownProfile.getEmail());

        Map<String, Object> content = new HashMap<>();
        if (executingWish.isAnonymously()) {
            content.put("firstName", null);
        } else {
            content.put("firstName", ownProfile.getFirstName());
        }

        content.put("wish", executingWish.getWish().getTitle());

        processNotification(ownProfile, "finish-wish-owner-template.flth", content);
    }

    @Override
    public void sendFinishFailedWishToExecutor(ExecutingWish executingWish) {
        LOGGER.debug("Send to executor profile information about failed" + executingWish.getExecutingProfile().getEmail());

        Map<String, Object> content = new HashMap<>();

        processNotification(executingWish.getExecutingProfile(), "finish-failed-wish-executor-template.flth", content);
    }

    @Override
    public void sendFinishFailedWishToOwner(ExecutingWish executingWish) {
        LOGGER.debug("Send to owner profile information about failed" + executingWish.getExecutingProfile().getEmail());

        Map<String, Object> content = new HashMap<>();
        if (executingWish.isAnonymously()) {
            content.put("firstName", null);
        } else {
            content.put("firstName", executingWish.getExecutingProfile().getFirstName());
        }
        content.put("wish", executingWish.getWish());

        processNotification(executingWish.getExecutingProfile(), "finish-failed-wish-owner-template.flth", content);

    }

    private void processNotification(String email, String template, Map<String, Object> model) {

        if (StringUtils.isNotBlank(email)) {
            NotificationMessage notificationMessage = notificationTemplateService.createNotificationMessage(template, model);
            notificationMessage.setDestinationAddress(email);
            try {
                Future<Boolean> booleanFuture = notificationSenderService.sendNotification(notificationMessage);
                Boolean result = booleanFuture.get();
                if (!result) {
                    throw new RuntimeException("Failed to send email notification");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to send email notification");
            }
        } else {
            LOGGER.error("Notification ignored: destinationAddress is not exist" + email);
            throw new RuntimeException("Notification ignored: destinationAddress is not exist" + email);
        }
    }

    private void processNotification(Profile profile, String templateName, Map<String, Object> model) {

        String destinationAddress = notificationSenderService.getDestinationAddress(profile);

        if (StringUtils.isNotBlank(destinationAddress)) {
            NotificationMessage notificationMessage = notificationTemplateService.createNotificationMessage(templateName, model);
            notificationMessage.setDestinationAddress(destinationAddress);
            notificationMessage.setDestinationName(profile.getFirstName());
            try {
                Future<Boolean> booleanFuture = notificationSenderService.sendNotification(notificationMessage);
                Boolean result = booleanFuture.get();
                if (!result) {
                    throw new RuntimeException("Failed to send email notification");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to send email notification");
            }
        } else {
            LOGGER.error("Notification ignored: destinationAddress is empty for profile " + profile.getUid());
            throw new RuntimeException("Notification ignored: destinationAddress is empty for profile " + profile.getUid());
        }
    }
}
