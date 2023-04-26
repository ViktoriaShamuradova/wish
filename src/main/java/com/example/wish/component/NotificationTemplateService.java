package com.example.wish.component;

import com.example.wish.model.NotificationMessage;

import java.util.Map;

/**
 * Create NotificationMessage by template name and data model
 */
public interface NotificationTemplateService {

    NotificationMessage createNotificationMessage(String templateName, Map<String, Object> model);

}
