package com.example.wish.filter;

import com.example.wish.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {  //extends UsernamePasswordAuthenticationFilter{

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Autowired
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        String username = null;


        String requestUri = request.getRequestURI();
        if (requestUri.contains("/sign-in/google")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            JWT token = JWTParser.parse(jwt);
            JWTClaimsSet claimsSet = token.getJWTClaimsSet();

            String issuer = claimsSet.getIssuer();
            boolean isIssuerValid = "accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer);

            if (isIssuerValid) {
                GoogleIdToken idToken = googleIdTokenVerifier.verify(jwt);
                filterChain.doFilter(request, response);
                return;
            }
        } catch (IOException | GeneralSecurityException e) {
            // Error parsing or validating the token
            // Handle token validation error
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            log.error("Error logging in {}", e.getMessage());
            Map<String, String> errors = new HashMap<>();
            errors.put("token_error", e.getMessage());
            handleException(response, errors);

        } catch (Exception e) {
            log.error("Error logging in {}", e.getMessage());
            Map<String, String> errors = new HashMap<>();
            errors.put("token_error", e.getMessage());
            handleException(response, errors);
        }

        //if user authentificated than we don't need perform again all checks and updating the securityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { //means that user is not authenticated yet
            //if user not authenticated we need to check if user has in database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        }
    }

    private void handleException(HttpServletResponse response, Map<String, String> errors) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        new ObjectMapper().writeValue(response.getOutputStream(), errors);
    }
}
