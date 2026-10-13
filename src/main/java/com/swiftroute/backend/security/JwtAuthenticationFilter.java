
package com.swiftroute.backend.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.swiftroute.backend.model.AccountStatus;
import com.swiftroute.backend.model.Role;
import com.swiftroute.backend.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.isAccessTokenValid(token)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired access token");
            return;
        }

        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            String email = jwtService.extractEmail(token);

            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "User not found");
                return;
            }

            var user = userOptional.get();

            // Block suspended accounts, even if their token is still valid.
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Account is suspended");
                return;
            }

            // Block riders who have not been approved by an admin.
            if (user.getRole() == Role.RIDER
                    && !user.isRiderApproved()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Rider account is not approved");
                return;
            }

            String role = "ROLE_" + user.getRole().name();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority(role)));

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
