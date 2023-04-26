package com.example.wish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String destinationAddress;
    private String destinationName;
    private String subject;
    private String content;

    public NotificationMessage(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }
}