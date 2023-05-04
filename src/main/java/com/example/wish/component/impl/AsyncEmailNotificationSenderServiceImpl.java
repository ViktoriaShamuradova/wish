package com.example.wish.component.impl;

import com.example.wish.component.NotificationSenderService;
import com.example.wish.entity.Profile;
import com.example.wish.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
public class AsyncEmailNotificationSenderServiceImpl implements NotificationSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncEmailNotificationSenderServiceImpl.class);

    private final ExecutorService executorService;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${email.fromName}")
    private String fromName;

    @Value("${email.sendTryCount}")
    private int tryCount;


    @Override
    public Future<Boolean> sendNotification(NotificationMessage message) {
        return executorService.submit(new EmailItem(message, tryCount));
    }

    @Override
    public String getDestinationAddress(Profile profile) {
        return profile.getEmail();
    }


    private class EmailItem implements Callable<Boolean> {
        private final NotificationMessage notificationMessage;
        private int tryCount;

        private EmailItem(NotificationMessage notificationMessage, int tryCount) {
            super();
            this.notificationMessage = notificationMessage;
            this.tryCount = tryCount;
        }

        @Override
        public Boolean call() {
            try {
                LOGGER.debug("Send a new email to {}", notificationMessage.getDestinationAddress());
                MimeMessageHelper message = new MimeMessageHelper(javaMailSender.createMimeMessage(), false);
                message.setSubject(notificationMessage.getSubject());
                message.setTo(new InternetAddress(notificationMessage.getDestinationAddress(), notificationMessage.getDestinationName()));
                message.setFrom(fromEmail, fromName);
                message.setText(notificationMessage.getContent(), true);
                MimeMailMessage msg = new MimeMailMessage(message);
                javaMailSender.send(msg.getMimeMessage());
                LOGGER.debug("Email to {} successful sent", notificationMessage.getDestinationAddress());
                return true;
            } catch (Exception e) {
                LOGGER.error("Can't send email to " + notificationMessage.getDestinationAddress() + ": " + e.getMessage(), e);
                tryCount--;
                if (tryCount > 0) {
                    LOGGER.debug("Decrement tryCount and try again to send email: tryCount={}, destinationEmail={}", tryCount, notificationMessage.getDestinationAddress());
                    executorService.submit(this);
                } else {
                    LOGGER.error("Email not sent to " + notificationMessage.getDestinationAddress());
                   // throw new MailAuthenticationException(e.getMessage());
                    return false;
                }
            }
            return false;
        }
    }
}
