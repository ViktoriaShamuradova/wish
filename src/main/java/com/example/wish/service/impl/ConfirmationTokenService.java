package com.example.wish.service.impl;

import com.example.wish.entity.ConfirmationToken;
import com.example.wish.entity.Profile;
import com.example.wish.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository tokenRepository;

    public void save(ConfirmationToken confirmationToken) {
        tokenRepository.save(confirmationToken);
    }

    @Transactional(readOnly = true)
    public Optional<ConfirmationToken> find(String token) {
        return tokenRepository.findByToken(token);
    }
    @Transactional(readOnly = true)
    public int countByToken(String token) {
        return tokenRepository.countByToken(token);
    }

    @Transactional
    public int setConfirmedAt(String token) {
        return tokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }

    @Transactional
    public void deleteToken(Profile profile) {
        tokenRepository.deleteByProfile(profile);
    }

}
