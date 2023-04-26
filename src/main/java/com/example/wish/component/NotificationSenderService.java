package com.example.wish.component;

import com.example.wish.entity.Profile;
import com.example.wish.model.NotificationMessage;

/**
 * Service send notifications.
 * Implementations can send email, mobile phone notifications.
 * Implementations can get email, phone address.
 */
public interface NotificationSenderService {

    void sendNotification(NotificationMessage message);

    String  getDestinationAddress(Profile profile);
}
