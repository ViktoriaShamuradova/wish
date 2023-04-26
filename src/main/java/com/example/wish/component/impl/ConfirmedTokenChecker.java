package com.example.wish.component.impl;

import com.example.wish.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * удаляет токены, которые подтверждены
 */

@RequiredArgsConstructor
@Component
public class ConfirmedTokenChecker {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteConfirmedToken() {
        confirmationTokenRepository.deleteByCreatedAtIsNotNull();
    }
}
