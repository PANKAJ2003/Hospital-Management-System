package com.pms.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class RoleHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("In RoleHeaderAuthenticationFilter: " + request.getHeader("X-User-Role") + request.getHeader("X-User-Id"));

        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");
        if (role == null || userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "X-User-Role header is required");
            return;
        }

        PreAuthenticatedAuthenticationToken authentication =
                new PreAuthenticatedAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}