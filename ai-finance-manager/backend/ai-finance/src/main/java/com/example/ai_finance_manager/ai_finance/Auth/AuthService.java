package com.example.ai_finance_manager.ai_finance.Auth;


import com.example.ai_finance_manager.ai_finance.Auth.dto.AuthResponse;
import com.example.ai_finance_manager.ai_finance.Auth.dto.LoginRequest;
import com.example.ai_finance_manager.ai_finance.Auth.dto.RefreshRequest;
import com.example.ai_finance_manager.ai_finance.Auth.dto.SignupRequest;
import com.example.ai_finance_manager.ai_finance.Entity.User;
import com.example.ai_finance_manager.ai_finance.Security.CustomUserDetailsService;
import com.example.ai_finance_manager.ai_finance.Security.JwtService;
import com.example.ai_finance_manager.ai_finance.enums.Role;
import com.example.ai_finance_manager.ai_finance.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse signup(SignupRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .gender(request.gender())
                .phone(request.phone())
                .role(Role.USER)
                .build();

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException(email);
        }
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return buildResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        String email;
        try {
            email = jwtService.extractUsername(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));
        UserDetails userDetails = userDetailsService.toUserDetails(user);

        if (!jwtService.isRefreshTokenValid(token, userDetails)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        UserDetails userDetails = userDetailsService.toUserDetails(user);
        return new AuthResponse(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails),
                user.getName(),
                user.getGender());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}