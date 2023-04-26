package com.example.wish.component.impl;

import com.example.wish.component.NotificationContentResolver;
import com.example.wish.component.NotificationTemplateService;
import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTemplateServiceImpl.class);
    private Map<String, NotificationMessage> notificationTemplates;

    private final NotificationContentResolver notificationContentResolver;


    @Override
    public NotificationMessage createNotificationMessage(String templateName, Map<String, Object> model) {
        NotificationMessage message = notificationTemplates.get(templateName);
        if (message == null) {
            throw new CantCompleteClientRequestException("Notification template '" + templateName + "' not found");
        }

        String resolvedContent = notificationContentResolver.resolve(templateName, model);
        message.setContent(resolvedContent);

        return message;
    }


    @PostConstruct
    private void postConstruct() {
        notificationTemplates = Collections.unmodifiableMap(getNotificationTemplates());
        LOGGER.info("Loaded {} notification templates", notificationTemplates.size());
    }


    private Map<String, NotificationMessage> getNotificationTemplates() {
//        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
//        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
//
//        reader.setValidating(false);
//        reader.loadBeanDefinitions(new PathResource(notificationConfigPath));

        Map<String, NotificationMessage> notificationsTemplates = new HashMap<>();

        NotificationMessage notificationMessageExecuteWishOwner = new NotificationMessage();
        notificationMessageExecuteWishOwner.setSubject("hooray! Someone execute your wish!");
        notificationsTemplates.put("execute-wish-owner-template.flth", notificationMessageExecuteWishOwner);

        NotificationMessage notificationMessageExecuteWishExecutor = new NotificationMessage();
        notificationMessageExecuteWishExecutor.setSubject("You execute wish");
        notificationsTemplates.put("execute-wish-executor-template.flth", notificationMessageExecuteWishExecutor);

        NotificationMessage notificationMessageConfirmWishOwner = new NotificationMessage();
        notificationMessageConfirmWishOwner.setSubject("Magic happens!");
        notificationsTemplates.put("finish-wish-owner-template.flth", notificationMessageConfirmWishOwner);

        NotificationMessage notificationMessageFinishFailedWishOwner = new NotificationMessage();
        notificationMessageFinishFailedWishOwner.setSubject("Something happened..");
        notificationsTemplates.put("finish-failed-wish-owner-template.flth", notificationMessageFinishFailedWishOwner);

        NotificationMessage notificationMessageFinishFailedWishExecutor = new NotificationMessage();
        notificationMessageFinishFailedWishExecutor.setSubject("Next time");
        notificationsTemplates.put("finish-failed-wish-executor-template.flth", notificationMessageFinishFailedWishExecutor);

        NotificationMessage notificationMessageConfirmSuccessWishExecutor = new NotificationMessage();
        notificationMessageConfirmSuccessWishExecutor.setSubject("Karma accrual");
        notificationsTemplates.put("confirm-wish-executor-success-template.flth", notificationMessageConfirmSuccessWishExecutor);

        NotificationMessage notificationMessageConfirmSuccessWishOwner = new NotificationMessage();
        notificationMessageConfirmSuccessWishOwner.setSubject("Karma accrual");
        notificationsTemplates.put("confirm-wish-owner-success-template.flth", notificationMessageConfirmSuccessWishOwner);

        NotificationMessage notificationMessageCancelExecutionWishOwner = new NotificationMessage();
        notificationMessageCancelExecutionWishOwner.setSubject("Cancel execution");
        notificationsTemplates.put("cancel-execution-wish-owner-template.flth", notificationMessageCancelExecutionWishOwner);

        NotificationMessage notificationMessageCancelExecutionWishExecutor = new NotificationMessage();
        notificationMessageCancelExecutionWishExecutor.setSubject("Cancel execution");
        notificationsTemplates.put("cancel-execution-wish-executor-template.flth", notificationMessageCancelExecutionWishExecutor);

        NotificationMessage notificationMessageConfirmFailedWishOwner = new NotificationMessage();
        notificationMessageConfirmFailedWishOwner.setSubject("Confirm failed");
        notificationsTemplates.put("confirm-wish-owner-failed-template.flth", notificationMessageConfirmFailedWishOwner);

        NotificationMessage notificationMessageConfirmFailedWishExecutor = new NotificationMessage();
        notificationMessageConfirmFailedWishExecutor.setSubject("Confirm failed");
        notificationsTemplates.put("confirm-wish-executor-failed-template.flth", notificationMessageConfirmFailedWishExecutor);

        NotificationMessage notificationMessageConfirmEmail = new NotificationMessage();
        notificationMessageConfirmEmail.setSubject("Confirm your email");
        notificationsTemplates.put("confirm-email-template.flth", notificationMessageConfirmEmail);

        NotificationMessage notificationMessageForgotPassword = new NotificationMessage();
        notificationMessageForgotPassword.setSubject("Forgot password");
        notificationsTemplates.put("forgot-password-template.flth", notificationMessageForgotPassword);

        //return beanFactory.getBeansOfType(NotificationMessage.class);
        return notificationsTemplates;
    }
}

