package com.example.wish.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

//@Component
//public class StartEndpointFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        System.out.println(request.getRequestURI());
//        if ("/boomerang/v1/demo/auth/start".equals(request.getRequestURI())) {
//            // Create a temporary authentication token
//            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
//
//            // Set the authentication token on the request
//            SecurityContextHolder.getContext().setAuthentication(auth);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
