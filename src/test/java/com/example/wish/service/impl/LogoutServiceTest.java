package com.example.wish.service.impl;

import com.example.wish.entity.Token;
import com.example.wish.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void logout_ValidToken_ExpiredAndRevoked() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // Mock the Authorization header
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");

        // Mock token repository behavior
        Token storedToken = new Token();
        Mockito.when(tokenRepository.findByToken("valid_token")).thenReturn(java.util.Optional.of(storedToken));

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        verify(tokenRepository, times(1)).save(storedToken);

        assertTrue(storedToken.isExpired());
        assertTrue(storedToken.isRevoked());
    }

    @Test
    void logout_InvalidToken_NoActionTaken() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // Mock the Authorization header with an invalid format
        Mockito.when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat invalid_token");

        // Act
        logoutService.logout(request, response, authentication);

        // Assert
        verify(tokenRepository, never()).findByToken(anyString());
        //verify(tokenRepository, never()).save(any);
    }

    @Test
    void logout_TokenNotFound_NoActionTaken() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        // Mock the Authorization header
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer non_existing_token");

        // Mock token repository behavior when token is not found
        Mockito.when(tokenRepository.findByToken("non_existing_token")).thenReturn(java.util.Optional.empty());

        // Act
        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).save(any());
    }
}
