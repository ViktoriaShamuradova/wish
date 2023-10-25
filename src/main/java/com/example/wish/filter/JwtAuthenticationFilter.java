package com.example.wish.filter;

import com.example.wish.repository.TokenRepository;
import com.example.wish.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Autowired
    private TokenRepository tokenRepository;

    /**
     * all exceptions that are thrown in this method need to be handled here.
     * they are not handled in ApiExceptionHandler
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (shouldBypassAuthentication(request, authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtToken(authHeader);
            if (isValidGoogleIdToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(jwt);
            if (username!= null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = loadUserByUsername(username);
                boolean isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);
                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    authenticateUser(request, userDetails);
                }
            }

        } catch (Exception e) {
            handleException(response, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldBypassAuthentication(HttpServletRequest request, String authHeader) {
        String requestUri = request.getRequestURI();
        return requestUri.contains("/sign-in/google") || authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private String extractJwtToken(String authHeader) {
        return authHeader.substring(7);
    }

    private boolean isValidGoogleIdToken(String jwt) {
        try {
            JWT token = JWTParser.parse(jwt);
            JWTClaimsSet claimsSet = token.getJWTClaimsSet();
            String issuer = claimsSet.getIssuer();
            return "accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer);
        } catch (ParseException e) {
            log.error("Error validating Google ID token: {}", e.getMessage());
            return false;
        }
    }

    private void authenticateUser(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.error("Error loading user by username: {}", e.getMessage());
            throw e;
        }
    }

    private void handleException(HttpServletResponse response, String errorMessage) throws IOException {
        log.error("Error logging in: {}", errorMessage);
        Map<String, String> errors = new HashMap<>();
        errors.put("token_error", errorMessage);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        new ObjectMapper().writeValue(response.getOutputStream(), errors);
    }
}
